package il.cshaifasweng.OCSFMediatorExample.client;

import il.cshaifasweng.OCSFMediatorExample.entities.Exam;
import il.cshaifasweng.OCSFMediatorExample.entities.Question;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ExamController {
    private final int ANSWER_NUM = 4;
    private String name;
    private Exam exam;
    private ArrayList<ComboBox<String>> answers_list;
    @FXML
    private Label examHeaderLabel;
    @FXML
    private Button answersBtn;
    @FXML
    private ScrollPane examPane;
    @FXML
    private VBox examContentPane;
    @FXML
    private Button idBtn;
    @FXML
    private TextField idTF;
    @FXML
    private Label idLabel;
    @FXML
    private Label instructionsLabel;
    @FXML
    private Label timerLabel;
    @FXML
    void initialize(){
        EventBus.getDefault().register(this);
    }
    @FXML
    void SubmitId(ActionEvent event) {
        String id = idTF.getText();
        String message = "StartExam " + id;
        System.out.println("the message is: " + message);//for debugging

        SimpleClient.sendMessage(message);
    }
    @Subscribe
    public void StartExam(StartExamEvent event){
        name = event.getName();
        exam = event.getExam();
        answersBtn.setVisible(true);
        answers_list = new ArrayList<>();
        Platform.runLater(() -> {
            idBtn.setVisible(false);
            idLabel.setVisible(false);
            idTF.setVisible(false);
            examPane.setVisible(true);
            timerLabel.setVisible(true);
            examHeaderLabel.setText("Answer the questions and submit the test before your time runs out!");
            instructionsLabel.setText(instructionsLabel.getText() + "\n" + exam.getStudentDesc());
            List<Question> questions = exam.getQuestions();
            for (int i = 0; i < questions.size(); i++){
                Question question = questions.get(i);
                String answers = question.getAnswers();
                Label question_label = new Label("Question " + (i + 1) + "\n" + question.getText());
                question_label.setStyle("-fx-font: 24px \"System\";");
                ComboBox<String> answer_select = new ComboBox<>();
                answer_select.setStyle("-fx-font: 16px \"System\";");
                for (int j = 1; j <= ANSWER_NUM; j++){
                    int index = answers.indexOf("Answer" + j);
                    index = answers.indexOf(" ",index) + 1;
                    if(j != ANSWER_NUM) answer_select.getItems().add(answers.substring(index,answers.indexOf("Answer" + (j + 1))));
                    else answer_select.getItems().add(answers.substring(index));
                }
                examContentPane.getChildren().add(question_label);
                examContentPane.getChildren().add(answer_select);
                answers_list.add(answer_select);
            }
        });
        final int[] remaining_time = {exam.getTime(),0};
        Timer myTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (remaining_time[1] == 0){
                    if (remaining_time[0] == 0){
                        myTimer.cancel();
                        SubmitAnswersForced();
                    }
                    else{
                        remaining_time[1] = 59;
                        remaining_time[0]--;
                    }
                }
                else{
                    remaining_time[1]--;
                }
                Platform.runLater(() -> {
                    timerLabel.setText("Remaining Time: ");
                    if (remaining_time[0] < 10) timerLabel.setText(timerLabel.getText() + "0");
                    timerLabel.setText(timerLabel.getText() + remaining_time[0] + ":");
                    if (remaining_time[1] < 10) timerLabel.setText(timerLabel.getText() + "0");
                    timerLabel.setText(timerLabel.getText() + remaining_time[1]);

                });
            }
        };
        myTimer.scheduleAtFixedRate(task,0,1000);

    }
    @FXML
    void SubmitAnswersOnTime(ActionEvent event) {
        SubmitAnswers();
    }
    void SubmitAnswersForced() {
        SubmitAnswers();
    }
    void SubmitAnswers() {
        String answers = "";
        for (int i = 0; i < answers_list.size(); i++){
            answers += (answers_list.get(i).getSelectionModel().getSelectedIndex() + 1) + " ";
        }
        String message = "SubmitAnswers " + name + " " + exam.getId() + " " + (exam.getTime() - Integer.parseInt(timerLabel.getText().substring(16,18))) + " " + answers;
        System.out.println("the message is: " + message);//for debugging
        SimpleClient.sendMessage(message);
        EventBus.getDefault().post(new SuccessEvent("Your test was submitted successfully"));
        EventBus.getDefault().post(new SwitchScreenEvent("student_primary"));
    }

}