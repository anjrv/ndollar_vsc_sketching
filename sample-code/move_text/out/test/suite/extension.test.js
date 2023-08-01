"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const assert = require("assert");
const vscode = require("vscode");
const sinon = require("sinon");
const path = require("path");
suite('Extension Test Suite', () => {
    vscode.window.showInformationMessage('Start all tests.');
    test('Moving first line to the 10th line in an empty document', async () => {
        const input1 = "0,0";
        const input2 = "0,100";
        const input3 = "9,0";
        const showInputBoxStub = sinon.stub(vscode.window, 'showInputBox');
        showInputBoxStub.onCall(0).resolves(input1);
        showInputBoxStub.onCall(1).resolves(input2);
        showInputBoxStub.onCall(2).resolves(input3);
        const doc = await vscode.workspace.openTextDocument({
            content: 'Hello World!'
        });
        const editor = await vscode.window.showTextDocument(doc);
        await vscode.commands.executeCommand('move_text.rectangle');
        assert.strictEqual(editor.document.lineCount, 10);
        assert.strictEqual(editor.document.lineAt(9).text, "Hello World!");
        await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
    });
    test('Moving the find_max function below the is_palindrome function in testFile.py', async () => {
        const input1 = "1,0";
        const input2 = "2,100";
        const input3 = "19,0";
        const showInputBoxStub = sinon.stub(vscode.window, 'showInputBox');
        showInputBoxStub.onCall(0).resolves(input1);
        showInputBoxStub.onCall(1).resolves(input2);
        showInputBoxStub.onCall(2).resolves(input3);
        const extensionPath = vscode.extensions.getExtension('undefined_publisher.move_text')?.extensionPath;
        if (!extensionPath) {
            throw new Error('Extension root not found.');
        }
        const testfile = path.join(extensionPath, 'src', 'test', 'testData', 'testFile.py');
        const doc = await vscode.workspace.openTextDocument(testfile);
        const editor = await vscode.window.showTextDocument(doc);
        await vscode.commands.executeCommand('move_text.rectangle');
        assert.strictEqual(editor.document.lineAt(19).text, "def find_max(numbers):");
        assert.strictEqual(editor.document.lineAt(20).text, "    return max(numbers)");
        await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
    });
    test('Moving the is_palindrome function close to the bottom so that the bottom text gets pushed down', async () => {
        const input1 = "0,0";
        const input2 = "16,100";
        const input3 = "30,0";
        const showInputBoxStub = sinon.stub(vscode.window, 'showInputBox');
        showInputBoxStub.onCall(0).resolves(input1);
        showInputBoxStub.onCall(1).resolves(input2);
        showInputBoxStub.onCall(2).resolves(input3);
        const extensionPath = vscode.extensions.getExtension('undefined_publisher.move_text')?.extensionPath;
        if (!extensionPath) {
            throw new Error('Extension root not found.');
        }
        const testfile = path.join(extensionPath, 'src', 'test', 'testData', 'testFile.py');
        const doc = await vscode.workspace.openTextDocument(testfile);
        const editor = await vscode.window.showTextDocument(doc);
        await vscode.commands.executeCommand('move_text.rectangle');
        assert.strictEqual(editor.document.lineCount, 55);
        await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
    });
    teardown(() => {
        sinon.restore();
    });
});
//# sourceMappingURL=extension.test.js.map