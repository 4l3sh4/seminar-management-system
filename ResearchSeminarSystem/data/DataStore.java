package data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import models.*;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<User> users = new ArrayList<>();
    public List<Student> students = new ArrayList<>();
    public List<Evaluator> evaluators = new ArrayList<>();
    public List<Coordinator> coordinators = new ArrayList<>();
    public List<Submission> submissions = new ArrayList<>();
    public List<Session> sessions = new ArrayList<>();
    public List<Evaluation> evaluations = new ArrayList<>();
    public List<Award> awards = new ArrayList<>();
}
