speciebox
=========

group savings using multisignature escrow

Running the demo of current wallet stuff.

1) run the SetupDemoWallet main class. It will do everything for you except put new coins in. 

2) To do that, you need to go to some testnet3 coin faucet, like http://tpfaucet.appspot.com/. and enter the address displayed in the console.

3) You will see that your wallet changes state and "owns bitcoin". Your wallet is configured to save every time it registers a change, so all you have to do is sit by for a few seconds to let things happen and hit "enter" to finish. You will see a new folder with all your .wallet, .tmp, .spvchain, and .sbx files. 

4) Run the ExampleRunner main. You can see all of the different options available to you.... in the worst switch statement ever because I'm lazy -simeon
