// The module 'vscode' contains the VS Codh extensibility API
// Import the module and reference it with the alias vscode in your code below
import * as vscode from 'vscode';
import { sketchingClient } from './sketchingClient';
import { ParseSketchRequest, Point, Stroke } from './sketching_api_pb';

// This method is called when your extension is activated
// Your extension is activated the very first time the command is executed
export function activate(context: vscode.ExtensionContext) {
  console.log('Congratulations, your extension "move_text" is now active!');

  context.subscriptions.push(
    vscode.commands.registerCommand('move_text.rectangle', async () => {
      const editor = vscode.window.activeTextEditor;
      let cordinates = [];
      cordinates.push(
        (
          await vscode.window.showInputBox({
            prompt: 'Enter cordinate 1',
            placeHolder: 'x,y',
          })
        )
          ?.split(',')
          .map(Number)
      );
      cordinates.push(
        (
          await vscode.window.showInputBox({
            prompt: 'Enter cordinate 2',
            placeHolder: 'x,y',
          })
        )
          ?.split(',')
          .map(Number)
      );
      cordinates.push(
        (
          await vscode.window.showInputBox({
            prompt: 'Enter moving cordinate',
            placeHolder: 'x,y',
          })
        )
          ?.split(',')
          .map(Number)
      );

      console.log(
        await sketchingClient.parseSketch(
          new ParseSketchRequest().setStrokesList([
            new Stroke().setPointsList([
              new Point().setX(cordinates[0]![0]).setY(cordinates[0]![1]),
              new Point().setX(cordinates[1]![0]).setY(cordinates[1]![1]),
              new Point().setX(cordinates[2]![0]).setY(cordinates[2]![1]),
            ]),
          ])
        )
      );

      if (
        cordinates[0]?.length === 2 &&
        cordinates[1]?.length === 2 &&
        cordinates[2]?.length === 2
      ) {
        const pos1 = new vscode.Position(cordinates[0][0], cordinates[0][1]);
        const pos2 = new vscode.Position(cordinates[1][0], cordinates[1][1]);
        let movePos = new vscode.Position(cordinates[2][0], cordinates[2][1]);

        if (editor) {
          let lines = [];
          const lineCount = editor.document.lineCount;
          const requiredlineCount = movePos.line + (pos2.line - pos1.line);
          const emptyLinesRequired = lineCount < requiredlineCount;
          const edit = new vscode.WorkspaceEdit();

          //replace text
          for (
            let currentLine = pos1.line;
            currentLine <= pos2.line;
            currentLine++
          ) {
            const currentPos1 = new vscode.Position(
              currentLine,
              pos1.character
            );
            const currentPos2 = new vscode.Position(
              currentLine,
              pos2.character
            );
            const line = editor.document.getText(
              new vscode.Range(currentPos1, currentPos2)
            );
            lines.push(line);
            edit.replace(
              editor.document.uri,
              new vscode.Range(currentPos1, currentPos2),
              ''
            );
          }

          //insert the text at desired position
          //text exceeds linecount and is the bottom text
          if (emptyLinesRequired && movePos.line >= lineCount) {
            let counter = 0;
            for (
              let currentLine = lineCount;
              currentLine <= requiredlineCount;
              currentLine++
            ) {
              if (currentLine >= movePos.line) {
                const line = lines[counter];
                counter += 1;
                edit.insert(
                  editor.document.uri,
                  new vscode.Position(currentLine, 0),
                  '\n' + line
                );
              } else {
                edit.insert(
                  editor.document.uri,
                  new vscode.Position(currentLine, 0),
                  '\n'
                );
              }
            }
          }

          //text exceeds linecount but is not bottom text
          else if (emptyLinesRequired && movePos.line < lineCount) {
            //remove all lines at the bottom so we can relocate them
            let bottomLines = [];
            for (
              let currentLine = lineCount;
              currentLine >= movePos.line;
              currentLine--
            ) {
              const currentPos1 = new vscode.Position(currentLine, 0);
              const currentPos2 = new vscode.Position(currentLine, 100);
              const line = editor.document.getText(
                new vscode.Range(currentPos1, currentPos2)
              );
              bottomLines.push(line);
              edit.delete(
                editor.document.uri,
                new vscode.Range(currentPos1, currentPos2)
              );
            }

            //add new lines
            let bigString = '';
            for (let i = 0; i < lines.length; i++) {
              bigString += lines[i] + '\n';
            }
            edit.insert(
              editor.document.uri,
              new vscode.Position(movePos.line, 0),
              bigString
            );
            let bottomBigString = '';
            for (let i = bottomLines.length - 1; i >= 0; i--) {
              if (i !== 0) {
                bottomBigString += bottomLines[i] + '\n';
              } else {
                bottomBigString += bottomLines[i];
              }
            }
            edit.insert(
              editor.document.uri,
              new vscode.Position(movePos.line, 0),
              bottomBigString
            );
          }

          //text does not exceed the linecount
          //TODO: Make it so that text does not overlap with other text
          else {
            for (let i = 0; i < lines.length; i++) {
              const line = lines[i];
              edit.insert(editor.document.uri, movePos, line);
              movePos = new vscode.Position(
                movePos.line + 1,
                movePos.character
              );
            }
          }
          await vscode.workspace.applyEdit(edit);
          vscode.window.showInformationMessage('Text successfully moved!');
        } else {
          vscode.window.showErrorMessage('No active editor found!');
        }
      } else {
        vscode.window.showErrorMessage('Inputs failed');
      }
    })
  );
}

// This method is called when your extension is deactivated
export function deactivate() {}
