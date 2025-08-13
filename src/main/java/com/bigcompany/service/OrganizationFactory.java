package com.bigcompany.service;
import com.bigcompany.model.Employee; import java.util.*; import java.util.function.*; import java.util.stream.*; public final class OrganizationFactory{
  private OrganizationFactory(){} public static Organization build(List<Employee> emps){ if(emps==null||emps.isEmpty()) throw new IllegalArgumentException("No employees found.");
    var byId=emps.stream().collect(Collectors.toMap(Employee::getId,Function.identity())); for(Employee e: emps){ if(e.getManagerId()!=null){ var m=byId.get(e.getManagerId()); if(m==null) throw new IllegalStateException("Manager id "+e.getManagerId()+" not found for "+e.getId()); m.getDirectReports().add(e);} }
    var ceos=emps.stream().filter(e->e.getManagerId()==null).toList(); if(ceos.size()!=1) throw new IllegalStateException("Expected exactly 1 CEO, found "+ceos.size()); var ceo=ceos.get(0);
    OrgGraph.validateAcyclic(byId); OrgGraph.validateAllReachCEO(byId, ceo);
    return new Organization(byId, ceo);
  }
  public record Organization(Map<Integer,Employee> byId, Employee ceo) {}
}