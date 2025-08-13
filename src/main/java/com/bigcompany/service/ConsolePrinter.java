package com.bigcompany.service;
import com.bigcompany.model.Employee; import com.bigcompany.service.rules.*; import java.util.*; public final class ConsolePrinter{
  public void print(RuleEngine.Result result){
    printUnderpaid(result.findings().stream().filter(f->f instanceof UnderpaidManagerFinding).map(f->(UnderpaidManagerFinding)f).toList());
    System.out.println(); printOverpaid(result.findings().stream().filter(f->f instanceof OverpaidManagerFinding).map(f->(OverpaidManagerFinding)f).toList());
    System.out.println(); printLongChains(result.findings().stream().filter(f->f instanceof ReportingLineTooLongFinding).map(f->(ReportingLineTooLongFinding)f).toList());
  }
  private void printUnderpaid(List<UnderpaidManagerFinding> list){ System.out.println("== Managers earning LESS than they should =="); if(list.isEmpty()){System.out.println("(none)"); return;} for(var md: list){ Employee m=md.subject(); System.out.printf("%d %s: short by %.2f (min=%.2f, avgSubs=%.2f)%n", m.getId(), m.getFullName(), md.shortBy(), md.minAllowed(), md.avgSubs()); } }
  private void printOverpaid(List<OverpaidManagerFinding> list){ System.out.println("== Managers earning MORE than they should =="); if(list.isEmpty()){System.out.println("(none)"); return;} for(var md: list){ Employee m=md.subject(); System.out.printf("%d %s: excess %.2f (max=%.2f, avgSubs=%.2f)%n", m.getId(), m.getFullName(), md.excess(), md.maxAllowed(), md.avgSubs()); } }
  private void printLongChains(List<ReportingLineTooLongFinding> list){ System.out.println("== Reporting lines too long (> 4 between employee and CEO) =="); if(list.isEmpty()){System.out.println("(none)"); return;} for(var ct: list){ Employee e=ct.subject(); System.out.printf("%d %s: too long by %d (between=%d)%n", e.getId(), e.getFullName(), ct.excess(), ct.betweenCount()); } }
}