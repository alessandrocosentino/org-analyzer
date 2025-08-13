package com.bigcompany.service;
import com.bigcompany.service.rules.*; import java.util.*; public final class RuleEngine{
  private final List<Rule<? extends Finding>> rules; public RuleEngine(Collection<? extends Rule<? extends Finding>> rules){ this.rules=List.copyOf(rules); }
  public record Result(List<Finding> findings){}
  public Result run(OrganizationFactory.Organization org){ List<Finding> all=new ArrayList<>(); for(Rule<? extends Finding> r: rules){ all.addAll(r.evaluate(org)); } return new Result(all); }
}