package gr.agro;

import java.util.List;

public class Employee {

    private String documentId;
    private String fullname;
    private String tel;
    private String rate_hour;
    private String start_date;
    private int is_active;
    private List<EmpFarm> empfarms;
    private List<Work> works;
    private double totalHoursPerWeek;
    private double totalSalaryPerWeek;

    public double getTotalSalaryPerWeek() {
        return totalSalaryPerWeek;
    }

    public void setTotalSalaryPerWeek(double totalSalaryPerWeek) {
        this.totalSalaryPerWeek = totalSalaryPerWeek;
    }

    public double getTotalHoursPerWeek() {
        return totalHoursPerWeek;
    }

    public void setTotalHoursPerWeek(double totalHoursPerWeek) {
        this.totalHoursPerWeek = totalHoursPerWeek;
    }

    public List<Work> getWorks() {
        return works;
    }

    public void setWorks(List<Work> works) {
        this.works = works;
    }

    public Employee() {
        // Empty constructor needed for Firestore deserialization
    }

    public List<EmpFarm> getEmpfarms() {
        return empfarms;
    }

    public void setEmpfarms(List<EmpFarm> empfarms) {
        this.empfarms = empfarms;
    }

    public int getIs_active() {
        return is_active;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getRate_hour() {
        return rate_hour;
    }

    public void setRate_hour(String rate_hour) {
        this.rate_hour = rate_hour;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getFullname() {
        return fullname;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

}