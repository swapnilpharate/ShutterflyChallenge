Calculating Customer Lifetime Value

One way to analyze acquisition strategy and estimate marketing cost is to calculate the Lifetime Value (“LTV”) of a customer. 
Simply speaking, LTV is the projected revenue that customer will generate during their lifetime.
Provided sample input data of events captured from shutterfly public sites in json array format in .txt file.Task is to an ingest given event data
and write analytics method to find top x customer with highest calculate the Lifetime Value (“LTV”).
Ingest(e, D)
Given event e, update data D

TopXSimpleLTVCustomers(x, D)
Return the top x customers with the highest Simple Lifetime Value from data D.

Directory structure

This Project contains required source code,librabries, test input data , output and sample data file. 
Project is developed using Java 1.7.

- src - (contains source code)
  |-
    com
     |-
       shutterfly
          |-
          |- interfaces (contains interfaces)
          |- main (contains main class MainClass.java)
          |- model (contains pojo classes)
    
- input ( contains test data)
- output( contains output file of test run)
- sample_input (contains sample data of each event type)
- referenced_librabries (contains required libraries)

Design decisions or Assumptions

This projects requires librabry javax.json-1.0.2 which is provided in referenced_librabries folded.
All data types for event filed are declared as String object and processed as per need.
Data set used for testing (input.txt) is close to complete for that timeframe 2/2/2016 -2/2/2017 . 
Missing Site_visit for given order is considered as one Site_visit assuming order cant be placed without visting site.
Customers are not distinguished as NEW Or UPDATE. But in future may required.
Weeks are calucated using actual time difference between two dates.


Reference

https://blog.kissmetrics.com/how-to-calculate-lifetime-value