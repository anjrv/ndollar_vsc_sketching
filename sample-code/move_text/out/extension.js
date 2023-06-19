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
                const requiredlineCount = movePos.line;
                const emptyLinesRequired = lineCount < requiredlineCount;
                const edit = new vscode.WorkspaceEdit();
                if (emptyLinesRequired) {
                    for (let currentLine = lineCount; currentLine < requiredlineCount - 1; currentLine++) {
                        edit.insert(editor.document.uri, new vscode.Position(currentLine, 0), '\n');
                    }
                }
                //replace text
                for (let currentLine = pos1.line; currentLine <= pos2.line; currentLine++) {
                    const currentPos1 = new vscode.Position(currentLine, pos1.character);
                    const currentPos2 = new vscode.Position(currentLine, pos2.character);
                    const line = editor.document.getText(new vscode.Range(currentPos1, currentPos2));
                    lines.push(line);
                    edit.replace(editor.document.uri, new vscode.Range(currentPos1, currentPos2), "");
                }
                //insert the text at desired position
                for (let i = 0; i < lines.length; i++) {
                    if (emptyLinesRequired) {
                        const line = lines[i];
                        edit.insert(editor.document.uri, movePos, "\n" + line);
                        movePos = new vscode.Position(movePos.line + 1, movePos.character);
                    }
                    else {
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
            vscode.window.showErrorMessage("Inputs failed");
        }
    }));
}
exports.activate = activate;
// This method is called when your extension is deactivated
function deactivate() { }
exports.deactivate = deactivate;
//# sourceMappingURL=extension.js.map