package _5_conditional_workflow;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrganizingTools {

    @Tool
    public Date getCurrentDate(){
        return new Date();
    }

    @Tool("finds the email addresses and names of people that need to be present at the onsite interview for a given job description ID")
    public List<String> getInvolvedEmployeesForInterview(@P("job description ID") String jobDescriptionId){
        // dummy implementation for demo
        return new ArrayList<>(List.of(
                "Anna Bolena: hiring.manager@company.com",
                "Chris Durue: near.colleague@company.com",
                "Esther Finnigan: vp@company.com"));
    }

    @Tool("creates agenda entries for employees based on email address")
    public void createCalendarEntry(@P("list of employee email addresses") List<String> emailAddress, @P("meeting topic") String topic, @P("start date and time in format yyyy-mm-dd hh:mm") String start, @P("end date and time in format yyyy-mm-dd hh:mm") String end){
        // dummy implementation for demo
        System.out.println("*** CALENDAR ENTRY CREATED ***");
        System.out.println("Topic: " + topic);
        System.out.println("Start: " + start);
        System.out.println("End: " + end);
    }

    @Tool
    public int sendEmail(@P("list of recipient email addresses") List<String> to, @P("list of CC email addresses") List<String> cc, @P("emailsubject") String subject, @P("body") String body){
        // dummy implementation for demo
        System.out.println("*** EMAIL SENT ***");
        System.out.println("To: " + to);
        System.out.println("Cc: " + cc);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
        return 1234; // dummy email ID
    }

    @Tool
    public void updateApplicationStatus(@P("job description ID") String jobDescriptionId, @P("candidate (first name, last name)") String candidateName, @P("new application status") String newStatus){
        // dummy implementation for demo
        System.out.println("*** APPLICATION STATUS UPDATED ***");
        System.out.println("Job Descirption ID: " + jobDescriptionId);
        System.out.println("Candidate Name: " + candidateName);
        System.out.println("New Status: " + newStatus);
    }
}
