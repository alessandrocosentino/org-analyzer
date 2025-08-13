package com.bigcompany.model;
import java.util.*; public final class Employee {
  private final int id; private final String firstName,lastName; private final double salary; private final Integer managerId; private final List<Employee> directReports=new ArrayList<>();
  public Employee(int id,String f,String l,double s,Integer m){this.id=id;this.firstName=f;this.lastName=l;this.salary=s;this.managerId=m;}
  public int getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;}
  public String getFullName(){return firstName+" "+lastName;} public double getSalary(){return salary;} public Integer getManagerId(){return managerId;}
  public List<Employee> getDirectReports(){return directReports;}
}