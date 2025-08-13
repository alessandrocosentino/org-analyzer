package com.bigcompany;
import com.bigcompany.io.*; import com.bigcompany.model.Employee; import com.bigcompany.service.*; import com.bigcompany.service.OrganizationFactory.Organization; import com.bigcompany.service.rules.*; import org.junit.jupiter.api.*; import org.junit.jupiter.api.io.TempDir; import java.io.*; import java.nio.file.*; import java.util.*; import static org.junit.jupiter.api.Assertions.*;
public class OrgAnalyzerRuleEngineTest {
  @TempDir Path tempDir;
  private RuleEngine.Result run(String csv) throws IOException {
    Path file=tempDir.resolve("employees.csv"); Files.writeString(file,csv); var reader=new EmployeeCsvReader(); List<Employee> employees=reader.read(file); Organization org=OrganizationFactory.build(employees);
    var policy=new DefaultSalaryPolicy(); var engine=new RuleEngine(List.of(new UnderpaidManagerRule(policy), new OverpaidManagerRule(policy), new ReportingLineTooLongRule(4))); return engine.run(org);
  }
  @Test void sampleCsv_parses_and_rules_apply() throws IOException {
    String csv=String.join("\n","Id,firstName,lastName,salary,managerId","123,Joe,Doe,60000.0,","124,Martin,Chekov,45000.0,123","125,Bob,Ronstad,47000.0,123","300,Alice,Hasacat,50000.0,124","305,Brett,Hardleaf,34000.0,300");
    var result=run(csv); boolean martinUnder=result.findings().stream().filter(f->f instanceof UnderpaidManagerFinding).map(f->(UnderpaidManagerFinding)f).anyMatch(md-> md.subject().getId()==124 && Math.abs(md.shortBy()-15000.0)<0.001); assertTrue(martinUnder);
    long over=result.findings().stream().filter(f->f instanceof OverpaidManagerFinding).count(); assertEquals(0, over);
    long longChains=result.findings().stream().filter(f->f instanceof ReportingLineTooLongFinding).count(); assertEquals(0, longChains);
  }
  @Test void long_chain_detected() throws IOException {
    String csv=String.join("\n","Id,firstName,lastName,salary,managerId","1,CEO,One,100000.0,","2,M2,Two,90000.0,1","3,M3,Three,80000.0,2","4,M4,Four,70000.0,3","5,M5,Five,60000.0,4","6,M6,Six,50000.0,5","7,Worker,Seven,40000.0,6");
    var result=run(csv); var finding=result.findings().stream().filter(f->f instanceof ReportingLineTooLongFinding).map(f->(ReportingLineTooLongFinding)f).findFirst().orElseThrow(); assertEquals(5, finding.betweenCount()); assertEquals(1, finding.excess());
  }
  @Test void boundaries_respected() throws IOException {
    String csv=String.join("\n","Id,firstName,lastName,salary,managerId","1,CEO,One,100000.0,","2,Mgr,Two,120.0,1","3,S1,Three,100.0,2","4,S2,Four,100.0,2");
    var result=run(csv); long over=result.findings().stream().filter(f->f instanceof OverpaidManagerFinding).count(); assertEquals(0, over);
    String overCsv=csv.replace("2,Mgr,Two,120.0,1", "2,Mgr,Two,160.0,1"); var overResult=run(overCsv);
    boolean overBy10=overResult.findings().stream().filter(f->f instanceof OverpaidManagerFinding).map(f->(OverpaidManagerFinding)f).anyMatch(md-> md.subject().getId()==2 && Math.abs(md.excess()-10.0)<0.001); assertTrue(overBy10);
  }
}