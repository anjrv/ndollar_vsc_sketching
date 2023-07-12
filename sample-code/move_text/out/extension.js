"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.deactivate = exports.activate = void 0;
const vscode = require("vscode");
function activate(context) {
    console.log('Congratulations, your extension "move_text" is now active!');
    context.subscriptions.push(vscode.commands.registerCommand('move_text.rectangle', async () => {
        const editor = vscode.window.activeTextEditor;
        let cordinates = [];
        cordinates.push((await vscode.window.showInputBox({ prompt: 'Enter cordinate 1', placeHolder: "x,y" }))?.split(',').map(Number));
        cordinates.push((await vscode.window.showInputBox({ prompt: 'Enter cordinate 2', placeHolder: "x,y" }))?.split(',').map(Number));
        cordinates.push((await vscode.window.showInputBox({ prompt: 'Enter moving cordinate', placeHolder: "x,y" }))?.split(',').map(Number));
        if ((cordinates[0]?.length === 2) && (cordinates[1]?.length === 2) && (cordinates[2]?.length === 2)) {
            const pos1 = new vscode.Position(cordinates[0][0], cordinates[0][1]);
            const pos2 = new vscode.Position(cordinates[1][0], cordinates[1][1]);
            let movePos = new vscode.Position(cordinates[2][0], cordinates[2][1]);
            if (editor) {
                let lines = [];
                const lineCount = editor.document.lineCount;
                const requiredlineCount = movePos.line + (pos2.line - pos1.line);
                const emptyLinesRequired = lineCount < requiredlineCount;
                const edit = new vscode.WorkspaceEdit();
                //replace selected text for ""
                for (let currentLine = pos1.line; currentLine <= pos2.line; currentLine++) {
                    const currentPos1 = new vscode.Position(currentLine, pos1.character);
                    const currentPos2 = new vscode.Position(currentLine, pos2.character);
                    const line = editor.document.getText(new vscode.Range(currentPos1, currentPos2));
                    lines.push(line);
                    edit.delete(editor.document.uri, new vscode.Range(currentPos1, currentPos2));
                }
                //insert the text at desired position
                //text exceeds linecount and is the bottom text
                if (emptyLinesRequired && movePos.line >= lineCount) {
                    let counter = 0;
                    for (let currentLine = lineCount; currentLine <= requiredlineCount; currentLine++) {
                        if (currentLine >= movePos.line) {
                            const line = lines[counter];
                            counter += 1;
                            edit.insert(editor.document.uri, new vscode.Position(currentLine, 0), '\n' + line);
                        }
                        else {
                            edit.insert(editor.document.uri, new vscode.Position(currentLine, 0), '\n');
                        }
                    }
                }
                //text exceeds linecount but is not bottom text
                else if (emptyLinesRequired && movePos.line < lineCount) {
                    //remove all lines at the bottom so we can relocate them
                    let bottomLines = [];
                    for (let currentLine = lineCount; currentLine >= movePos.line; currentLine--) {
                        const currentPos1 = new vscode.Position(currentLine, 0);
                        const currentPos2 = new vscode.Position(currentLine + 1, 0);
                        const line = editor.document.getText(new vscode.Range(currentPos1, currentPos2));
                        bottomLines.push(line);
                        edit.delete(editor.document.uri, new vscode.Range(currentPos1, currentPos2));
                    }
                    //Insert selected lines into new position
                    let selectedLines = "";
                    for (let i = 0; i < lines.length; i++) {
                        if (i !== lines.length - 1) {
                            selectedLines += lines[i] + "\n";
                        }
                        else {
                            selectedLines += lines[i];
                        }
                    }
                    edit.insert(editor.document.uri, new vscode.Position(movePos.line, 0), selectedLines);
                    let bottomLinesString = "";
                    for (let i = bottomLines.length - 1; i >= 0; i--) {
                        bottomLinesString += bottomLines[i];
                    }
                    edit.insert(editor.document.uri, new vscode.Position(movePos.line, 0), bottomLinesString);
                }
                //text does not exceed the linecount
                else {
                    for (let i = 0; i < lines.length; i++) {
                        const line = lines[i];
                        edit.insert(editor.document.uri, movePos, line);
                        movePos = new vscode.Position(movePos.line + 1, movePos.character);
                    }
                }
                await vscode.workspace.applyEdit(edit);
                vscode.window.showInformationMessage("Text successfully moved!");
            }
            else {
                vscode.window.showErrorMessage("No active editor found!");
            }
        }
        else {
            vscode.window.showErrorMessage("Inputs failed!");
        }
    }));
    // command that deletes all lines.
    context.subscriptions.push(vscode.commands.registerCommand('move_text.remove_all_lines', async () => {
        const editor = vscode.window.activeTextEditor;
        if (editor) {
            const lines = editor.document.lineCount;
            let edit = new vscode.WorkspaceEdit();
            for (let i = lines - 1; i >= 0; i--) {
                const currentPos1 = new vscode.Position(i, 0);
                const currentPos2 = new vscode.Position(i + 1, 0);
                const range = new vscode.Range(currentPos1, currentPos2);
                edit.delete(editor.document.uri, range);
            }
            await vscode.workspace.applyEdit(edit);
            vscode.window.showInformationMessage("Lines succesfully deleted!");
        }
        else {
            vscode.window.showErrorMessage("No active editor found!");
        }
    }));
}
exports.activate = activate;
// This method is called when your extension is deactivated
function deactivate() { }
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map