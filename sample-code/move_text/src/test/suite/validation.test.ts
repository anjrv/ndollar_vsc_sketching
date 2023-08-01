import * as vscode from 'vscode';
import * as sinon from 'sinon';



suite('Validation Test Suite', () => {
    vscode.window.showInformationMessage('Start all tests.');


    test('Invalid inputs by the user', async () => {

        const input1 = "hello world";
		const input2 = "123";
		const input3 = "---------";
	
		const showInputBoxStub = sinon.stub(vscode.window, 'showInputBox');
		showInputBoxStub.onCall(0).resolves(input1);
		showInputBoxStub.onCall(1).resolves(input2);
		showInputBoxStub.onCall(2).resolves(input3);
        const showErrorMessageStub = sinon.stub(vscode.window, 'showErrorMessage');

        const doc = await vscode.workspace.openTextDocument({
			content: 'Hello World!'
		});
	
		await vscode.window.showTextDocument(doc);
        await vscode.commands.executeCommand('move_text.rectangle');   
        sinon.assert.calledWith(showErrorMessageStub, 'Invalid input. Please provide valid values.');
        await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
    });


    test('No activeTextEditor found', async () => {

        const input1 = "0,0";
		const input2 = "0,100";
		const input3 = "9,0";
	
		const showInputBoxStub = sinon.stub(vscode.window, 'showInputBox');
		showInputBoxStub.onCall(0).resolves(input1);
		showInputBoxStub.onCall(1).resolves(input2);
		showInputBoxStub.onCall(2).resolves(input3);
        const showErrorMessageStub = sinon.stub(vscode.window, 'showErrorMessage');
        
        await vscode.commands.executeCommand('move_text.rectangle');
        
        sinon.assert.calledWith(showErrorMessageStub, 'No active text editor found! Select a text editor to continue.');
        await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
    });


    // test that tests the case where the user enters a line number that is out of bounds??
    test('Negative cordinates by the user', async () => {

        const input1 = "-1,0";
		const input2 = "5,100";
		const input3 = "20,0";
	
		const showInputBoxStub = sinon.stub(vscode.window, 'showInputBox');
		showInputBoxStub.onCall(0).resolves(input1);
		showInputBoxStub.onCall(1).resolves(input2);
		showInputBoxStub.onCall(2).resolves(input3);
        const showErrorMessageStub = sinon.stub(vscode.window, 'showErrorMessage');

        const doc = await vscode.workspace.openTextDocument({
			content: 'Hello World!'
		});
	
		await vscode.window.showTextDocument(doc);
        await vscode.commands.executeCommand('move_text.rectangle');   
        sinon.assert.calledWith(showErrorMessageStub, "Cordinates can't be negative!");
        await vscode.commands.executeCommand('workbench.action.closeActiveEditor');
    });
    


    teardown( () => {

		sinon.restore();

	});




    });