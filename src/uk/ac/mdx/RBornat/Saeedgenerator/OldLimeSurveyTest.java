package uk.ac.mdx.RBornat.Saeedgenerator;

import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringEscapeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OldLimeSurveyTest {

    public final Document doc;
    public final int surveyId = 97537; // number doesn't seem to matter
    public final int authorisationGroup, personalGroup, finishGroup;
    public final int[] programGroups; // for subdividing programming questions
           
    
    private int groupId = 0, questionId = 0, conditionId=0, 
        quotaId=0, quotaMemberId=0, quotaLanguageSettingsId,
        groupOrder = 0, questionOrder = 0, subquestionOrder=0;
    
    private final String exitURL = "http://www.eis.mdx.ac.uk/research/PhDArea/saeed/",
                         exitURLDescription = "Saeed Dehnadi's web page";
    
    OldLimeSurveyTest(Document doc, int nProgGroups, String[] surveyTitle, String[] surveyWelcome, String[] surveyGoodbye) {
        this.doc = doc;
        Element rootElement = doc.createElement("document");
        doc.appendChild(rootElement);

        rootElement.appendChild(newTextNode("LimeSurveyDocType", "Survey"));
        rootElement.appendChild(newTextNode("DBVersion", "155"));

        // languages
        Node languages = rootElement.appendChild(doc.createElement("languages"));
        languages.appendChild(newTextNode("language", "en"));

        // answers
        Node answers = rootElement.appendChild(doc.createElement("answers"));
        // within which fields and rows
        String[] answers_fieldnames = 
            new String[] {"qid", "code", "answer", "sortorder", "assessment_value", 
                "language", "scale_id"};
        appendFieldsAndBlankRows(answers, answers_fieldnames);
        
        // conditions
        Node conditions = rootElement.appendChild(doc.createElement("conditions"));
        // within which fields and rows
        String[] conditions_fieldnames = 
            new String[] {"cid", "qid", "scenario", "cqid", "cfieldname", 
                "method", "value"};
       appendFieldsAndBlankRows(conditions, conditions_fieldnames);
        
       // groups
        Node groups = rootElement.appendChild(doc.createElement("groups"));
        // within which fields and rows
        String[] groups_fieldnames = new String[] 
               {"gid", "sid", "group_name", "group_order", "description", 
                "language", "randomization_group", "grelevance"};
        appendFieldsAndBlankRows(groups, groups_fieldnames);
        
        // questions
        Node questions = rootElement.appendChild(doc.createElement("questions"));
        // within which fields and rows, each row a question
        String[] questions_fieldnames =
            new String[] {"qid", "parent_qid", "sid", "gid", "type", "title", "question", 
                "preg", "help", "other", "mandatory", "question_order", "language", 
                "scale_id", "same_default", "relevance"};
        appendFieldsAndBlankRows(questions, questions_fieldnames);
        
        // subquestions
        Node subquestions = rootElement.appendChild(doc.createElement("subquestions"));
        // within which fields and rows, each row a subquestion
        appendFieldsAndBlankRows(subquestions, questions_fieldnames);
        
        // question attributes
        Node questionAttributes = rootElement.appendChild(doc.createElement("question_attributes"));
        // within which fields
        // and then rows of attributes ... one per question at least, maybe one per subquestion
        String[] questionAttributes_fieldnames = new String[] {"qid", "attribute", "value"};
        appendFieldsAndBlankRows(questionAttributes, questionAttributes_fieldnames);
        /* subquestionAttributesRownames =
            new String[] {"hide_tip", "random_group", "page_break", "max_answers", 
                "min_answers", "exclude_all_others", "public_statistics", "hidden", 
                "other_numbers_only", "display_columns", "scale_export", "random_order", 
                "array_filter_exclude", "exclude_all_others_auto", "array_filter", 
                "assessment_value", "other_replace_text"};
                */
        
        // quotas (used to filter out those who don't want to be analysed)
        Node quota = rootElement.appendChild(doc.createElement("quota"));
        // within which fields and rows, each row a quota
        String[] quota_fieldnames =
            new String[] {"id", "sid", "name", "qlimit", "action", "active", "autoload_url"};
        appendFieldsAndBlankRows(quota, quota_fieldnames);
        // and quota_members
        Node quotaMembers = rootElement.appendChild(doc.createElement("quota_members"));
        // within which fields and rows, each row a quota member
        String[] quotaMembers_fieldnames =
            new String[] {"id", "sid", "qid", "quota_id", "code"};
        appendFieldsAndBlankRows(quotaMembers, quotaMembers_fieldnames);
        // and quota_members
        Node quotaLanguageSettings = rootElement.appendChild(doc.createElement("quota_languagesettings"));
        // within which fields and rows, each row a quota language setting :-)
        String[] quotaLanguageSettings_fieldnames =
            new String[] {
                "quotals_id", "quotals_quota_id", "quotals_language", "quotals_name", 
                "quotals_message", "quotals_url", "quotals_urldescrip"};
        appendFieldsAndBlankRows(quotaLanguageSettings, quotaLanguageSettings_fieldnames);
        
        /* // get the introductory questions. There don't have to be any.
        BufferedReader introin = 
            (BufferedReader)Generator.openFile(
                    FileDialog.LOAD, "Introductory questions", "LimeSurveyIntroDir", "LimeSurveyIntroFile", 
                                    "LimeSurveyIntroductoryQuestions.txt");
        Introductory intro = new Introductory(introin); */
        
        // the authorisation group
        authorisationGroup = ++groupId;
        appendCDATARow(findFirstChild(groups, "rows"), 
                new String[] {
                "gid", Integer.toString(authorisationGroup), 
                "sid", Integer.toString(surveyId),
                "group_name", "authorisation", 
                "group_order", Integer.toString(groupOrder++), 
                "description", "Accept questionnaire conditions", 
                "language", "en", 
                "randomization_group", "", 
                "grelevance", ""});
        
        int qAuth = ++questionId;
        appendQuestionRow(
                new String[] {
                        "qid", Integer.toString(qAuth),
                        "parent_qid", "0",
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(authorisationGroup),
                        "type", "L",
                        "title", "auth",
                        "question", "<p>The results of this questionnaire will be stored in a database " +
                        	"and will be analysed statistically by researchers investigating " +
                        	"the learning of programming.</p>" +
                        	"<p>&nbsp;</p>" +
                        	"<p>Your results will never be released to anybody in such a way " +
                        	"that you can be identified.</p>" +
                                "<p>&nbsp;</p>" +
                        	"<p>Please choose one of the following options:</p>",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "Y",
                        "question_order", "0",
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", "1"});
        appendQuestionAttributesRows(Integer.toString(qAuth),
                new String[] {
                "display_columns", "1",
                "page_break", "1",
                "hide_tip", "1"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qAuth),
                "code", "Y",
                "answer", "I am happy to allow my answers to be used in this way",
                "sortorder", "1",
                "assessment_value", "0", 
                "language", "en",
                "scale_id", "0"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qAuth),
                "code", "N",
                "answer", "I do not consent to use of my answers",
                "sortorder", "2",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        int quotaUnwilling = ++quotaId;
        appendCDATARow(findFirstChild(quota, "rows"),
                new String[] {
                "id", Integer.toString(quotaUnwilling),
                "sid", Integer.toString(surveyId),
                "name", "Unwilling",
                "qlimit", "0",
                "action", "1",
                "active", "1",
                "autoload_url", "0"
        });
        int quotaUnwillingMember = ++quotaMemberId; 
        appendCDATARow(findFirstChild(quotaMembers, "rows"),
                new String[] {
                "id", Integer.toString(quotaUnwillingMember),
                "sid", Integer.toString(surveyId),
                "qid", Integer.toString(qAuth),
                "quota_id", Integer.toString(quotaUnwilling),
                "code", "N"
        });
        int quotaUnwillingLanguageSettings = ++quotaLanguageSettingsId; 
        appendCDATARow(findFirstChild(quotaLanguageSettings, "rows"),
                new String[] {
                "quotals_id", Integer.toString(quotaUnwillingLanguageSettings),
                "quotals_quota_id", Integer.toString(quotaUnwilling),
                "quotals_language", "en",
                "quotals_name", "Unwilling",
                "quotals_message", "We're sorry you felt unable to participate.",
                "quotals_url", exitURL,
                "quotals_urldescrip", exitURLDescription
        });
        
        // The personal group
        personalGroup = ++groupId;
        appendCDATARow(findFirstChild(groups, "rows"), 
                new String[] {
                "gid", Integer.toString(personalGroup), 
                "sid", Integer.toString(surveyId),
                "group_name", "personal", 
                "group_order", Integer.toString(groupOrder++), 
                "description", "Background information about you", 
                "language", "en", 
                "randomization_group", "", 
                "grelevance", ""});
        
        questionOrder = 0;
        int qName = ++questionId;
        appendQuestionRow(                
                new String[] {
                "qid", Integer.toString(qName),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "S",
                "title", "Name",
                "question", "Name",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"});
        appendQuestionAttributesRows(Integer.toString(qName),
                new String[] {
                "hide_tip", "1",
                /*"location_mapheight", "300",
                "location_mapwidth", "500",
                "location_mapzoom", "11",*/
                "text_input_width", "50",
                "time_limit_action", "1"
        });
        
        int qAge = ++questionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qAge),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "S",
                "title", "Age",
                "question", "Age",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"});
        appendQuestionAttributesRows(Integer.toString(qAge),
                new String[] {
                "hide_tip", "1",
                /*"location_mapheight", "300",
                "location_mapwidth", "500",
                "location_mapzoom", "11",*/
                "numbers_only", "1",
                "text_input_width", "50",
                "time_limit_action", "1"
        });
        
        int qGender = ++questionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qGender),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "L",
                "title", "Gender",
                "question", "Gender",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"});
        appendQuestionAttributesRows(
                Integer.toString(qGender),
                new String[] {
                "hide_tip", "1",
                "display_columns", "3"
        });
        
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qGender),
                "code", "M",
                "answer", "Male",
                "sortorder", "1",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qGender),
                "code", "F",
                "answer", "Female",
                "sortorder", "2",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        
        int qQualifications = ++questionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qQualifications),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "Q",
                "title", "Qualifications",
                "question", "Previous qualifications (please list -- sorry the boxes are so small):",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"});
        appendQuestionAttributesRows(
                Integer.toString(qQualifications),
                new String[] {
                "assessment_value", "1"
        });
        
       subquestionOrder = 0;
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qQualifications),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "QualA",
                        "question", "A level",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qQualifications),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "QualG",
                        "question", "GCSE (or O level)",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qQualifications),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "QualO",
                        "question", "Other",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
       
        int qProgrammer = ++questionId;
        String cProgrammerCFN = surveyId+"X"+personalGroup+"X"+qProgrammer;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qProgrammer),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "L",
                "title", "Programmer",
                "question", "Have you ever written a computer program " +
                            "in any programming language?",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"});
        appendQuestionAttributesRows(Integer.toString(qProgrammer),
                new String[] {
                "hide_tip", "1",
                "display_columns", "3"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qProgrammer),
                "code", "Y",
                "answer", "Yes",
                "sortorder", "1",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qProgrammer),
                "code", "N",
                "answer", "No",
                "sortorder", "2",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qProgrammer),
                "code", "Q",
                "answer", "Not Sure",
                "sortorder", "3",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
       
        int qLanguage = ++questionId;
        int cLanguage = ++conditionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qLanguage),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "M",
                "title", "Language",
                "question", "In what programming language(s) have you written programs?",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "("+cProgrammerCFN+".NAOK == \"Y\")"
        });
        appendQuestionAttributesRows(Integer.toString(qLanguage),
                new String[] {
                "hide_tip", "1",
                "display_columns", "6",
                "assessment_value", "1",
                "other_replace_text", "Other",
                "other", "Y"
        });
       
        appendConditionRow(
                new String[] {
                    "cid", Integer.toString(cLanguage),
                    "qid", Integer.toString(qLanguage),
                    "scenario", "1",
                    "cqid", Integer.toString(qProgrammer),
                    "cfieldname", cProgrammerCFN,
                    "method", "==   ",
                    "value", "Y"});
        
        subquestionOrder = 0;
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "Basic",
                        "question", "Basic",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C",
                        "question", "C",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "Java",
                        "question", "Java",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "Cplus",
                        "question", "C++",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "VB",
                        "question", "Visual Basic",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "Fort",
                        "question", "Fortran",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qLanguage),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "Other",
                        "question", "Other",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});

        int qCourse = ++questionId;
        String cCourseCFN = surveyId+"X"+personalGroup+"X"+qCourse;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qCourse),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "L",
                "title", "Course",
                "question", "This questionnaire comes as part of a programming course. " +
                            "Have you studied programming before the beginning of this course?",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "Y",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"}); 
       appendQuestionAttributesRows(Integer.toString(qCourse),
               new String[] {
               "hide_tip", "1",
               "display_columns", "3"
       });
       appendAnswerRow(
               new String[] {
               "qid", Integer.toString(qCourse),
               "code", "Y",
               "answer", "Yes",
               "sortorder", "1",
               "assessment_value", "1", 
               "language", "en",
               "scale_id", "0"
       });
       appendAnswerRow(
               new String[] {
               "qid", Integer.toString(qCourse),
               "code", "N",
               "answer", "No",
               "sortorder", "2",
               "assessment_value", "1", 
               "language", "en",
               "scale_id", "0"
       });
       appendAnswerRow(
               new String[] {
               "qid", Integer.toString(qCourse),
               "code", "Q",
               "answer", "Not Sure",
               "sortorder", "3",
               "assessment_value", "1", 
               "language", "en",
               "scale_id", "0"
       });
        
        int qPrevious = ++questionId;
        int cPrevious = ++conditionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qPrevious),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "Q",
                "title", "Previous",
                "question", "What programming course(s) did you take?",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "N",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "("+cCourseCFN+".NAOK == \"Y\")"
        }); 
        appendQuestionAttributesRows(Integer.toString(qPrevious),
                new String[] {
                "hide_tip", "1"
        });

        appendConditionRow(
                new String[] {
                    "cid", Integer.toString(cPrevious),
                    "qid", Integer.toString(qPrevious),
                    "scenario", "1",
                    "cqid", Integer.toString(qCourse),
                    "cfieldname", cCourseCFN,
                    "method", "==   ",
                    "value", "Y"});
        subquestionOrder = 0;
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qPrevious),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C1",
                        "question", "First course",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qPrevious),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C2",
                        "question", "Second course",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qPrevious),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C3",
                        "question", "Third course",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        
        int qPassFail = ++questionId;
        int cPassFail = ++conditionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qPassFail),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(personalGroup),
                "type", "F",
                "title", "PassFail",
                "question", "And did you pass or fail?",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "N",
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "("+cCourseCFN+".NAOK == \"Y\")"
        }); 
        appendQuestionAttributesRows(Integer.toString(qPassFail),
                new String[] {
                "hide_tip", "1"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qPassFail),
                "code", "PP",
                "answer", "Pass",
                "sortorder", "1",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qPassFail),
                "code", "PF",
                "answer", "Fail",
                "sortorder", "2",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });
        appendAnswerRow(
                new String[] {
                "qid", Integer.toString(qPassFail),
                "code", "PQ",
                "answer", "Can't remember",
                "sortorder", "3",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
        });

        appendConditionRow(
                new String[] {
                    "cid", Integer.toString(cPassFail),
                    "qid", Integer.toString(qPassFail),
                    "scenario", "1",
                    "cqid", Integer.toString(qCourse),
                    "cfieldname", cCourseCFN,
                    "method", "==   ",
                    "value", "Y"});
        subquestionOrder = 0;
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qPassFail),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C1",
                        "question", "First course",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qPassFail),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C2",
                        "question", "Second course",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""});
        appendSubquestionRow(
                new String[] {
                        "qid", Integer.toString(++questionId),
                        "parent_qid", Integer.toString(qPassFail),
                        "sid", Integer.toString(surveyId),
                        "gid", Integer.toString(personalGroup),
                        "type", "T",
                        "title", "C3",
                        "question", "Third course",
                        "preg", "",
                        "help", "", 
                        "other", "N",
                        "mandatory", "",
                        "question_order", Integer.toString(subquestionOrder++),
                        "language", "en",
                        "scale_id", "0",
                        "same_default", "0",
                        "relevance", ""
                });
        
        // the programs group of questions
        programGroups = new int[nProgGroups];
        for (int i=0; i<nProgGroups; i++) {
            programGroups[i] = ++groupId;
            appendCDATARow(findFirstChild(groups, "rows"), 
                    new String[] {
                    "gid", Integer.toString(programGroups[i]), 
                    "sid", Integer.toString(surveyId),
                    "group_name", "programs"+(i+1), 
                    "group_order", Integer.toString(groupOrder++), 
                    "description", "Questionnaire", 
                    "language", "en", 
                    "randomization_group", "", 
                    "grelevance", ""
            });
        }

        // the finish group
        finishGroup = ++groupId;
        appendCDATARow(findFirstChild(groups, "rows"), 
                new String[] {
                "gid", Integer.toString(finishGroup), 
                "sid", Integer.toString(surveyId),
                "group_name", "finish", 
                "group_order", Integer.toString(groupOrder++), 
                "description", "", 
                "language", "en", 
                "randomization_group", "", 
                "grelevance", ""});
        
        int qFin = ++questionId;
        appendQuestionRow(
                new String[] {
                "qid", Integer.toString(qFin),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(finishGroup),
                "type", "U",
                "title", "finished",
                "question", "<p>You've reached the end of the questionnaire.</p>" +
                		"<p>&nbsp;</p>" +
                		"<p>If you want to leave any comments, please type them in the box below.</p>" +
                                "<p>&nbsp;</p>" +
                                "<p>When you've finished, press the &quot;Submit&quot; button to " +
                                "record your answers.&nbsp;</p>" +
                                "<p>&nbsp;</p>" +
                                "<p>If you want to go back to your answers, use the " +
                                "&quot;Previous&quot; button.</p>" +
                                "<p>&nbsp;</p>" +
                                "<p>If you want to delete all your answers, press " +
                                "&quot;Exit and clear survey&quot;.</p>",
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "N",
                "question_order", "0",
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"});

        // surveys
        Node surveys = rootElement.appendChild(doc.createElement("surveys"));
        // within which fields
        String[] surveys_fields =
            new String[] {
                "sid", Integer.toString(surveyId), 
                "admin", "Richard Bornat", 
                "startdate", "",  
                "expires", "",  
                "adminemail", "richard@bornat.me.uk",
                "anonymized", "N",
                "faxto", "",
                "format", "G", 
                "savetimings", "Y", 
                "template", "default", 
                "language", "en", 
                "additional_languages", "", 
                "datestamp", "N", // maybe should be Y?
                "usecookie", "N",
                "allowregister", "N", 
                "allowsave", "Y", 
                "printanswers", "N", 
                "autonumber_start", "0",
                "autoredirect", "N", 
                "showxquestions", "N", 
                "showgroupinfo", "X", 
                "shownoanswer", "N",
                "showqnumcode", "X", 
                "showwelcome", "Y", 
                "allowprev", "Y", 
                "ipaddr", "N", 
                "refurl", "N",
                "listpublic", "N", 
                "publicstatistics", "N", 
                "publicgraphs", "N", 
                "htmlemail", "Y",
                "tokenanswerspersistence", "N", 
                "assessments", "N", 
                "usecaptcha", "D", 
                "bouncetime", "", 
                "bounceprocessing", "N", 
                "bounceaccounttype", "", 
                "bounceaccounthost", "", 
                "bounceaccountuser", "", 
                "bounceaccountpass", "",
                "bounceaccountencryption", "", 
                "usetokens", "N", 
                "bounce_email", "R.Bornat@mdx.ac.uk", 
                "attributedescriptions", "", 
                "emailresponseto", "", 
                "emailnotificationto", "", 
                "tokenlength", "15", 
                "showprogress", "Y", 
                "allowjumps", "N", 
                "navigationdelay", "0",
                "nokeyboard", "N", 
                "alloweditaftercompletion", "N", 
                "googleanalyticsstyle", "", 
                "googleanalyticsapikey", ""};
        appendFieldsAndCDATARow(surveys, surveys_fields);
        
        // surveys language settings
        Node surveysLanguageSettings = rootElement.appendChild(doc.createElement("surveys_languagesettings"));
        // within which fields
        String[] surveysLanguageSettings_fields =
            new String[] {
                "surveyls_survey_id", Integer.toString(surveyId), 
                "surveyls_language", "en", 
                "surveyls_title", TextUtils.enLine(surveyTitle)+" (generated "+
                                            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                                                    Calendar.getInstance().getTime())+
                                                    ")", 
                "surveyls_description", TextUtils.enParaHTML(surveyTitle), 
                "surveyls_welcometext", TextUtils.enParaHTML(surveyWelcome), 
                "surveyls_url", exitURL, 
                "surveyls_urldescription", exitURLDescription, 
                "surveyls_endtext", TextUtils.enParaHTML(surveyGoodbye), 
                "surveyls_email_invite_subj", "Invitation to participate in a survey", 
                "surveyls_email_invite", "Dear {FIRSTNAME},<br /><br />" +
                		"you have been invited to participate in a survey.<br /><br />" +
                		"The survey is titled:<br />\"{SURVEYNAME}\"<br /><br />" +
                		"\"{SURVEYDESCRIPTION}\"<br /><br />" +
                		"To participate, please click on the link below.<br /><br />" +
                		"Sincerely,<br /><br />{ADMINNAME} ({ADMINEMAIL})<br /><br />" +
                		"----------------------------------------------<br />" +
                		"Click here to do the survey:<br />{SURVEYURL}<br /><br />" +
                		"If you do not want to participate in this survey and don't want " +
                		"to receive any more invitations please click the following link:" +
                		"<br />{OPTOUTURL}",
                "surveyls_email_remind_subj", "Reminder to participate in a survey",
                "surveyls_email_remind", "Dear {FIRSTNAME},<br /><br />" +
                "Recently we invited you to participate in a survey.<br /><br />" +
                "We note that you have not yet completed the survey, and wish to " +
                "remind you that the survey is still available should you wish to " +
                "take part.<br /><br />" +
                "The survey is titled:<br />\"{SURVEYNAME}\"<br /><br />" +
                "\"{SURVEYDESCRIPTION}\"<br /><br />" +
                "To participate, please click on the link below.<br /><br />" +
                "Sincerely,<br /><br />{ADMINNAME} ({ADMINEMAIL})<br /><br />" +
                "----------------------------------------------<br />" +
                "Click here to do the survey:<br />{SURVEYURL}<br /><br />" +
                "If you do not want to participate in this survey and don't want to " +
                "receive any more invitations please click the following link:<br />" +
                "{OPTOUTURL}",
                "surveyls_email_register_subj", "Survey registration confirmation",
                "surveyls_email_register", "Dear {FIRSTNAME},<br /><br />" +
                		"You, or someone using your email address, have registered to " +
                		"participate in an online survey titled {SURVEYNAME}.<br /><br />" +
                		"To complete this survey, click on the following URL:<br /><br />" +
                		"{SURVEYURL}<br /><br />" +
                		"If you have any questions about this survey, or if you did not " +
                		"register to participate and believe this email is in error, " +
                		"please contact {ADMINNAME} at {ADMINEMAIL}.",
                "surveyls_email_confirm_subj", "Confirmation of your participation in our survey",
                "surveyls_email_confirm", "Dear {FIRSTNAME},<br /><br />" +
                		"this email is to confirm that you have completed the survey " +
                		"titled {SURVEYNAME} and your response has been saved. Thank you " +
                		"for participating.<br /><br />" +
                		"If you have any further questions about this email, please " +
                		"contact {ADMINNAME} on {ADMINEMAIL}.<br /><br />" +
                		"Sincerely,<br /><br />{ADMINNAME}",
                "surveyls_dateformat", "5", 
                "email_admin_notification_subj", "Response submission for survey {SURVEYNAME}",
                "email_admin_notification", "Hello,<br /><br />" +
                		"A new response was submitted for your survey '{SURVEYNAME}'.<br /><br />" +
                		"Click the following link to reload the survey:<br />{RELOADURL}<br /><br />" +
                		"Click the following link to see the individual response:<br />" +
                		"{VIEWRESPONSEURL}<br /><br />" +
                		"Click the following link to edit the individual response:<br />" +
                		"{EDITRESPONSEURL}<br /><br />" +
                		"View statistics by clicking here:<br />{STATISTICSURL}",
                "email_admin_responses_subj", "Response submission for survey {SURVEYNAME} with results",
                "email_admin_responses", "<style type=\"text/css\">"+TextUtils.LineSep +
                		".printouttable {"+TextUtils.LineSep +
                		"  margin:1em auto;"+TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		".printouttable th {"+TextUtils.LineSep +
                		"  text-align: center;"+TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		".printouttable td {"+TextUtils.LineSep +
                		"  border-color: #ddf #ddf #ddf #ddf;"+TextUtils.LineSep +
                		"  border-style: solid;"+TextUtils.LineSep +
                		"  border-width: 1px;"+TextUtils.LineSep +
                		"  padding:0.1em 1em 0.1em 0.5em;"+TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		TextUtils.LineSep +
                		".printouttable td:first-child {"+TextUtils.LineSep +
                		"  font-weight: 700;"+TextUtils.LineSep +
                		"  text-align: right;"+TextUtils.LineSep +
                		"  padding-right: 5px;"+TextUtils.LineSep +
                		"  padding-left: 5px;"+TextUtils.LineSep +
                		TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		".printouttable .printanswersquestion td{"+TextUtils.LineSep +
                		"  background-color:#F7F8FF;"+TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		TextUtils.LineSep +
                		".printouttable .printanswersquestionhead td{"+TextUtils.LineSep +
                		"  text-align: left;"+TextUtils.LineSep +
                		"  background-color:#ddf;"+TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		TextUtils.LineSep +
                		".printouttable .printanswersgroup td{"+TextUtils.LineSep +
                		"  text-align: center;"+TextUtils.LineSep +
                		"  font-weight:bold;"+TextUtils.LineSep +
                		"  padding-top:1em;"+TextUtils.LineSep +
                		"}"+TextUtils.LineSep +
                		"</style>Hello,<br /><br />" +
                		"A new response was submitted for your survey '{SURVEYNAME}'.<br /><br />" +
                		"Click the following link to reload the survey:<br />{RELOADURL}<br /><br />" +
                		"Click the following link to see the individual response:<br />" +
                		"{VIEWRESPONSEURL}<br /><br />" +
                		"Click the following link to edit the individual response:<br />" +
                		"{EDITRESPONSEURL}<br /><br />" +
                		"View statistics by clicking here:<br />" +
                		"{STATISTICSURL}<br /><br /><br />" +
                		"The following answers were given by the participant:<br />" +
                		"{ANSWERTABLE}",
                "surveyls_numberformat", "0"};
        appendFieldsAndCDATARow(surveysLanguageSettings, surveysLanguageSettings_fields);
    }
        
    /*class Introductory {
        String authorisation[]=null, authorisationOptions[][] = null,
                finalisation[]=null, finalisationOptions[][] = null;
        Vector<LSIntroQuestion> qs = new Vector<LSIntroQuestion>(8);
        String line;
        
        Introductory(BufferedReader introin) {
            boolean authorised = false, finalised = false;
            try {
                line=markedReadLine(introin);
                while (line!=null) {
                    if (line.startsWith("**Authorisation") || line.startsWith("**Authorization")) {
                        if (authorised)
                            Utils.crash("two Authoris(z)ation questions");
                        if (qs.size()!=0)
                            Utils.crash("Authoris(z)ation question must come first");
                        qs.add(inputAuthQuestion(introin, line));
                        authorised = true;
                    }
                    else {
                        if (finalised)
                            Utils.crash("Final question must come last: followed instead by "+line);
                        if (line.startsWith("**Text")) 
                            qs.add(inputTextQuestion(introin, line));
                        else 
                        if (line.startsWith("**MultiText")) 
                            qs.add(inputMultiTextQuestion(introin, line));
                        else 
                        if (line.startsWith("**Number")) 
                            qs.add(inputNumberQuestion(introin, line));
                        else 
                        if (line.startsWith("**Choice")) 
                            qs.add(inputChoiceQuestion(introin, line));
                        else 
                        if (line.startsWith("**Final")) {
                            qs.add(inputFinalQuestion(introin, line));
                            finalised = true;
                        }
                        else
                            Utils.crash("cannot recognise control line "+line);
                    }

                    line=markedReadLine(introin);
                }
                
                iQs = qs.toArray(new LSIntroQuestion[qs.size()]);
            } catch (IOException e) {
                Utils.showErrorAlert("How can there be an IO error ("+e+") while reading lines of the test?");
                System.exit(1);
            }
            
        }
    }*/
    
    public Element newTextNode(String name, String value) {
        Element child = doc.createElement(name);
        child.appendChild(doc.createTextNode(value));
        return child;
    }
    
    public void appendFields(Element parent, String[] fieldnames) {
        Node fields = doc.createElement("fields");
        parent.appendChild(fields);
      for (String f : fieldnames) {
          Element fieldname = doc.createElement("fieldname");
          fieldname.appendChild(doc.createTextNode(f));
        }
    }
    
    public void appendFieldsAndBlankRows(Node parent, String[] fieldnames) {
        Node fields = doc.createElement("fields");
        parent.appendChild(fields);
        Node rows = doc.createElement("rows");
        parent.appendChild(rows);
        for (String f : fieldnames) {
            Node field = doc.createElement("fieldname");
            fields.appendChild(field);
            field.appendChild(doc.createTextNode(f));
        }
    }
    
    public void appendFieldsAndCDATARow(Node parent, String[] fieldnamesAndValues) {
        Node fields = doc.createElement("fields");
        parent.appendChild(fields);
        Node rows = doc.createElement("rows");
        parent.appendChild(rows);
        for (int i=0; i<fieldnamesAndValues.length; i+=2) {
            Node field = doc.createElement("fieldname");
            fields.appendChild(field);
            field.appendChild(doc.createTextNode(fieldnamesAndValues[i]));
        }
        appendCDATARow(rows, fieldnamesAndValues);
    }
    
    public void appendCDATARow(Node rows, String[] fieldnamesAndValues) {
        Node row = doc.createElement("row");
        rows.appendChild(row);
        for (int i=0; i<fieldnamesAndValues.length; i+=2) {
            Node value = doc.createElement(fieldnamesAndValues[i]);
            row.appendChild(value);
            value.appendChild(doc.createCDATASection(fieldnamesAndValues[i+1]));
        }
    }
    
    public void appendCDATARow(String nodename, String[] fieldnamesAndValues) {
        Node node = findFirstChild(doc.getDocumentElement(), nodename);
        Node rows = findFirstChild(node, "rows");
        appendCDATARow(rows, fieldnamesAndValues);
    }

    public void appendQuestionRow(String[] q) {
        appendCDATARow("questions", q);
    }
    
    public void appendSubquestionRow(String[] sq) {
        appendCDATARow("subquestions", sq);
    }
    
    public void appendConditionRow(String[] c) {
       appendCDATARow("conditions", c);
    }
    
    public void appendAnswerRow(String[] a) {
       appendCDATARow("answers", a);
    }
    
    public void apppendProgramsQuestion(int qnum /* ordinal not idx */, TestQuestion q, AnswerPage ap, int progIdx) {
        Node questions = findFirstChild(doc.getDocumentElement(), "questions");
        Node qrows = findFirstChild(questions, "rows");
        
        String[] qlines = PaperQuestionnaire.stringOfQuestion(q).split(TextUtils.LineSep);
        for (int i=0; i<qlines.length; i++)
            if (qlines[i].equals(""))
                qlines[i] = "<div>"+TextUtils.LineSep+TextUtils.LineSep+"&nbsp;</div>"+TextUtils.LineSep;
            else
                qlines[i] = "<div>"+TextUtils.LineSep+TextUtils.LineSep+qlines[i]+"</div>"+TextUtils.LineSep;
        
        questionId++;
        String qidString = Integer.toString(questionId);
        
        appendCDATARow(qrows,
                new String[] {
                "qid", qidString,
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(programGroups[progIdx]),
                "type", "M",
                "title", Integer.toString(qnum),
                "question", Integer.toString(qnum)+". "+TextUtils.interpolateStrings(qlines,TextUtils.LineSep),
                "preg", "",
                "help", "", 
                "other", "Y",
                "mandatory", "N",
                "question_order", Integer.toString(questionId-1),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"
        });
        
        // and then question attributes
        Node attributes = findFirstChild(doc.getDocumentElement(), "question_attributes");
        Node arows = findFirstChild(attributes, "rows");
        
        appendQuestionAttributesRows(arows, qidString,
                new String[] {
                "hide_tip", "1",
                "random_group", "",
                "page_break", "1",
                "max_answers", "",
                "min_answers", "",
                "exclude_all_others", "",
                "public_statistics", "0",
                "hidden", "0",
                "other_numbers_only", "0",
                "display_columns", "2",
                "scale_export", "0",
                "random_order", "1",
                "array_filter_exclude", "",
                "exclude_all_others_auto", "0",
                "array_filter", "",
                "assessment_value", "1",
                "other_replace_text", PaperQuestionnaire.stringOfOther(q)
        });
        
        // and then subquestions
        Node subquestions = findFirstChild(doc.getDocumentElement(), "subquestions");
        Node sqrows = findFirstChild(subquestions, "rows");
        SimpleSet<State> cs = ap.choices;
        
        for (int i=0; i<cs.size(); i++) {
            questionId++;
            String sqidString = Integer.toString(i+1);
            /* System.out.println("subquestion "+sqidString);
            System.out.flush();
            System.out.println(Generator.stringOfStatePairs(cs.item(i)));
            System.out.flush(); */
            appendCDATARow(sqrows,
                    new String[] {
                    "qid", Integer.toString(questionId),
                    "parent_qid", qidString,
                    "sid", Integer.toString(surveyId),
                    "gid", Integer.toString(programGroups[progIdx]),
                    "type", "T",
                    "title", sqidString,
                    "question", Generator.stringOfStatePairs(cs.item(i)),
                    "preg", "",
                    "help", "", 
                    "other", "N",
                    "mandatory", "",
                    "question_order", sqidString,
                    "language", "en",
                    "scale_id", "0",
                    "same_default", "0",
                    "relevance", ""
            });        
        }
    }
    
    public void appendQuestionAttributesRows(Node rows, String sqid, String[] avs) {
        for (int i=0; i<avs.length; i+=2) {
            appendCDATARow(rows, 
                    new String[] {
                    "qid", sqid,
                    "attribute", avs[i],
                    "value", avs[i+1]
            });
        }
    }
    
    public void appendQuestionAttributesRows(String sqid, String[] avs) {
        Node attributes = findFirstChild(doc.getDocumentElement(), "question_attributes");
        Node rows = findFirstChild(attributes, "rows");

        appendQuestionAttributesRows(rows, sqid, avs);
    }
    
    // utility methods
    public Node findFirstChild(Node n, String s) {
        NodeList ns = n.getChildNodes();
        for (int i=0; i<ns.getLength(); i++)
            if (ns.item(i).getNodeName().equals(s))
                return ns.item(i);
        Utils.crash("Node "+n.getNodeName()+" doesn't have a "+s+" child");
        return null; // can't happen
    }
    
    public Node findFirstChild(Element e, String s) {
        if (!(e instanceof Node))
            Utils.crash("findFirstChild given non-Node "+e);
        return findFirstChild((Node) e, s);
    }
    
    public void writeToFile(File f) {
        try { //write the document into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");

            DOMSource source = new DOMSource(doc);

            StreamResult result =  new StreamResult(f);
            transformer.transform(source, result);
        } catch(TransformerException tfe){
            System.err.println("Crash (TransformerException)");
            tfe.printStackTrace();
            Utils.crash("TransformerException "+tfe+" while printing LimeSurvey survey");
        }
    }

}
