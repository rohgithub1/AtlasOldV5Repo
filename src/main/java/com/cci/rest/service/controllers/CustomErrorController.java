/**
 * Custom Error Controller added from Server side to redirect
 * to main URL when error occurs or page not found (Addition to
 * Client side)
 * Redirects to /rpa component of React
 */

package com.cci.rest.service.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

 private static final String PATH = "/error";

 @RequestMapping(value = PATH)
 public String error() {
  return "redirect:/";
 }

 @Override
 public String getErrorPath() {
  return PATH;
 }
}
