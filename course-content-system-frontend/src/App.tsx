import { Route, Routes } from "react-router-dom";
import "./App.css";
import { Button } from "./components/ui/button";
import MainScreen from "./screens/MainScreen";

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<MainScreen />} />
      </Routes>
    </>
  );
}

export default App;
