import logo from './logo.svg';
import './App.css';
import Layout from './layout/Layout';
import MainRoutes from './MainRoutes';
import React, { useState } from "react";
function App() {
  const [alerts, setAlerts] = useState([]);

  const ws = new WebSocket("ws://127.0.0.1:9000/alerts/subscribe");

  ws.onmessage = function (event) {
    const json = JSON.parse(event);
    try {
      console.log(json);
    } catch (err) {
      console.log(err);
    }
  };

  return (
    <div className="App">
     <MainRoutes/>
    </div>
  );
}

export default App;
