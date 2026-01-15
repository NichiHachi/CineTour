import React from "react";
import "./Navbar.css";
import Button from "../Buttons/Button";
import Glow from "../Glow/Glow";
import StaggeredText from "../TextEffects/StaggeredText/StaggeredText";
import { useCookies } from "react-cookie";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import MenuOpenIcon from "@mui/icons-material/MenuOpen";
import SearchIcon from "@mui/icons-material/Search";
import Searchbar from "../Searchbar/Searchbar";

function Navbar() {
  const [cookies] = useCookies(["username"]);
  return (
    <Glow className="navbar">
      <div className="navbar-section">
        <div className="navbar-left">
          <Button
            children={
              <div className="icon">
                <MenuOpenIcon />
              </div>
            }
            onClick="toggleLeftBar()"
            id="toggle-left-bar-button"
          />
          <h1 className="logo">
            <a href="/">CineTour</a>
          </h1>
        </div>

        <div className="navbar-center">
          <div className="navbar-searchbar">
            <Searchbar />
          </div>
        </div>

        <div className="navbar-right">
          {cookies.username ? (
            <a href="/profile" className="navbar-user">
              <Button
                children={
                  <div className="icon">
                    <SearchIcon />
                  </div>
                }
                onClick="toggleSearchbar()"
                id="toggle-searchbar-button"
              />
              <Glow className="navbar-user-icon">
                <div className="user-icon">
                  <AccountCircleIcon />
                </div>
              </Glow>
              <div className="navbar-username"></div>
              {cookies.username}
            </a>
          ) : (
            <>
              <Button
                children={
                  <div className="icon">
                    <SearchIcon />
                  </div>
                }
                onClick="toggleSearchbar()"
                id="toggle-searchbar-button"
              />
              <Button
                children={
                  <a href="/login">
                    <StaggeredText>Se connecter</StaggeredText>
                  </a>
                }
              />
              <Button
                children={
                  <a href="/signup">
                    <StaggeredText>S'inscrire</StaggeredText>
                  </a>
                }
              />
            </>
          )}
        </div>
      </div>
    </Glow>
  );
}

export default Navbar;
