// Java Program to Illustrate DemoController File 

// Importing package in this code module 
package com.example.demo.controller; 
// Importing required classes 
import org.springframework.web.bind.annotation.RestController; 
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class DemoController { 

	@GetMapping("/hello") 
	public String helloGFG() 
	{ 
		return "Hello GeeksForGeeks"; 
	} 
}
