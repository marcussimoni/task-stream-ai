const { app, BrowserWindow } = require('electron');
const path = require('path');
const { spawn } = require('child_process');

let mainWindow;
let backendProcess;

function startBackend() {
    const backendPath = app.isPackaged
        ? path.join(process.resourcesPath, 'backend-binary')
        : path.join(__dirname, 'builds', 'task-stream-ai');

    console.log("checking path: ", backendPath)
    
    // Launch the GraalVM binary
    backendProcess = spawn(backendPath, ['--server.port=1234']);

    backendProcess.stdout.on('data', (data) => console.log(`Backend: ${data}`));
}

async function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1280,
        height: 800,
        title: "TaskStream AI",
        webPreferences: {
            nodeIntegration: false, // Security best practice
            contextIsolation: true
        }
    });

    let splash = new BrowserWindow({
        width: 500,
        height: 300,
        transparent: true,
        frame: false,
        alwaysOnTop: true,
        resizable: false,
        webPreferences: { nodeIntegration: false }
    });
    splash.loadFile(path.join(__dirname, 'splash.html'));
    console.log("Forcing splash screen visibility...");
    await new Promise(resolve => setTimeout(resolve, 5000));

    // Attempt to load the URL with a retry mechanism
    const loadURL = () => {
        return mainWindow.loadURL('http://localhost:1234')
            .then(() => {
                // Connection successful!
                if (splash) {
                    splash.destroy();
                    splash = null;
                }
                mainWindow.show();
            })
            .catch(() => {
                console.log('Backend not ready yet, retrying...');
                // Wait 1 second before the next retry attempt
                return new Promise(resolve => setTimeout(resolve, 1000)).then(loadURL);
            });
    };

    loadURL();

}

app.whenReady().then(() => {
    startBackend();
    createWindow();
});

// Clean up: stop the backend when Electron closes
app.on('window-all-closed', () => {
    if (backendProcess) backendProcess.kill();
    app.quit();
});