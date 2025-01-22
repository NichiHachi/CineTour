// package com.polytech.utils;

// import jakarta.servlet.http.Cookie;

// public class Cookies {
//     private String name;
//     private String value;

//     public Cookies(String name, String value) {
//         this.name = name;
//         this.value = value;
//     }

//     public String getName() {
//         return name;
//     }

//     public String getValue() {
//         return value;
//     }

//     public void setName(String name) {
//         this.name = name;
//     }

//     public void setValue(String value) {
//         this.value = value;
//     }

//     public Cookie createCookie(String name, String value) {
//         // create a cookie
//         Cookie cookie = new Cookie("username", "Jovan");
//         cookie.setMaxAge(7 * 24 * 60 * 60); // expires in 7 days
//         cookie.setSecure(true);
//         cookie.setHttpOnly(true);
//         cookie.setPath("/"); // global cookie accessible every where

//         return cookie;
//     }
// }
