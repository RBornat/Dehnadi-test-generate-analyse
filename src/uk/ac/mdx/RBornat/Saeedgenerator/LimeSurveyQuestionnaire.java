package uk.ac.mdx.RBornat.Saeedgenerator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.javatuples.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * We refer to the output of the generator as a questionnaire. LimeSurvey (and SurveyMonkey) 
 * talk of surveys. It would be innappropriate to change all the identifiers in this class
 * to talk of questionnaires.
 */
public class LimeSurveyQuestionnaire {

    public final Document doc;
    public final int surveyId = 97537; // number doesn't seem to matter, but this one works anyway
    // public final int[] programGroups; // for subdividing programming questions
           
    
    private int groupId = 0, currentGroup = 0,
            questionId = 0, conditionId=0, 
            quotaId=0, quotaMemberId=0, quotaLanguageSettingsId,
            groupOrder = 0, questionOrder = 0, subquestionOrder=0;
    
    private boolean finalised = false;
    
    // this should be in the survey description
    private final String exitURL = "http://www.eis.mdx.ac.uk/research/PhDArea/saeed/",
                         exitURLDescription = "Saeed Dehnadi's web page";
    
    private HashMap<String,Pair<Integer,Integer>> questionMap = new HashMap<String,Pair<Integer,Integer>>(20);
    
    LimeSurveyQuestionnaire(Document doc, Questionnaire questionnaire) {
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
                
        processQuestions(questionnaire.questions, null);
        
        // boilerplate: unavoidable, I think. Should be parameterised with administrator name and email
        // surveys
        Node surveys = rootElement.appendChild(doc.createElement("surveys"));
        // within which fields
        String[] surveys_fields =
                new String[] {
                "sid", Integer.toString(surveyId), 
                "admin", "Richard Bornat", 
                "startdate", "",  
                "expires", "",  
                "adminemail", "R.Bornat@mdx.ac.uk",
                "anonymized", "N",
                "faxto", "",
                "format", "G", 
                "savetimings", "Y", 
                "template", "default", 
                "language", "en", 
                "additional_languages", "", 
                "datestamp", "Y", // we need the date stamp, don't we?
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
                "surveyls_title", questionnaire.title+" (generated "+
                        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                                Calendar.getInstance().getTime())+
                                ")", 
                "surveyls_description", questionnaire.title, 
                "surveyls_welcometext", TextUtils.enParaHTML(questionnaire.welcome), 
                "surveyls_url", exitURL, 
                "surveyls_urldescription", exitURLDescription, 
                "surveyls_endtext", TextUtils.enParaHTML(questionnaire.goodbye), 
                "surveyls_email_invite_subj", "" /* "Invitation to participate in a survey" */ , 
                "surveyls_email_invite", "" /* "Dear {FIRSTNAME},<br /><br />" +
                    "you have been invited to participate in a survey.<br /><br />" +
                    "The survey is titled:<br />&quot;{SURVEYNAME}&quot;<br /><br />" +
                    "&quot;{SURVEYDESCRIPTION}&quot;<br /><br />" +
                    "To participate, please click on the link below.<br /><br />" +
                    "Sincerely,<br /><br />{ADMINNAME} ({ADMINEMAIL})<br /><br />" +
                    "----------------------------------------------<br />" +
                    "Click here to do the survey:<br />{SURVEYURL}<br /><br />" +
                    "If you do not want to participate in this survey and don't want " +
                    "to receive any more invitations please click the following link:" +
                    "<br />{OPTOUTURL}"*/ ,
                "surveyls_email_remind_subj", "" /* "Reminder to participate in a survey" */,
                "surveyls_email_remind", "" /* "Dear {FIRSTNAME},<br /><br />" +
                        "Recently we invited you to participate in a survey.<br /><br />" +
                        "We note that you have not yet completed the survey, and wish to " +
                        "remind you that the survey is still available should you wish to " +
                        "take part.<br /><br />" +
                        "The survey is titled:<br />&quot;{SURVEYNAME}&quot;<br /><br />" +
                        "&quot;{SURVEYDESCRIPTION}&quot;<br /><br />" +
                        "To participate, please click on the link below.<br /><br />" +
                        "Sincerely,<br /><br />{ADMINNAME} ({ADMINEMAIL})<br /><br />" +
                        "----------------------------------------------<br />" +
                        "Click here to do the survey:<br />{SURVEYURL}<br /><br />" +
                        "If you do not want to participate in this survey and don't want to " +
                        "receive any more invitations please click the following link:<br />" +
                        "{OPTOUTURL}" */ ,
                "surveyls_email_register_subj", "" /* "Survey registration confirmation" */,
                "surveyls_email_register", "" /* "Dear {FIRSTNAME},<br /><br />" +
                        "You, or someone using your email address, have registered to " +
                        "participate in an online survey titled {SURVEYNAME}.<br /><br />" +
                        "To complete this survey, click on the following URL:<br /><br />" +
                        "{SURVEYURL}<br /><br />" +
                        "If you have any questions about this survey, or if you did not " +
                        "register to participate and believe this email is in error, " +
                        "please contact {ADMINNAME} at {ADMINEMAIL}." */ ,
                "surveyls_email_confirm_subj", "" /* "Confirmation of your participation in our survey" */,
                "surveyls_email_confirm", "" /* "Dear {FIRSTNAME},<br /><br />" +
                        "this email is to confirm that you have completed the survey " +
                        "titled {SURVEYNAME} and your response has been saved. Thank you " +
                        "for participating.<br /><br />" +
                        "If you have any further questions about this email, please " +
                        "contact {ADMINNAME} on {ADMINEMAIL}.<br /><br />" +
                        "Sincerely,<br /><br />{ADMINNAME}" */,
                "surveyls_dateformat", "5", 
                "email_admin_notification_subj", "" /* "Response submission for survey {SURVEYNAME}" */,
                "email_admin_notification", "" /* "Hello,<br /><br />" +
                        "A new response was submitted for your survey '{SURVEYNAME}'.<br /><br />" +
                        "Click the following link to reload the survey:<br />{RELOADURL}<br /><br />" +
                        "Click the following link to see the individual response:<br />" +
                        "{VIEWRESPONSEURL}<br /><br />" +
                        "Click the following link to edit the individual response:<br />" +
                        "{EDITRESPONSEURL}<br /><br />" +
                        "View statistics by clicking here:<br />{STATISTICSURL}" */,
                "email_admin_responses_subj", "" /* "Response submission for survey {SURVEYNAME} with results" */,
                "email_admin_responses", "" /* "<style type=&quot;text/css&quot;>"+Utils.LineSep +
                        ".printouttable {"+Utils.LineSep +
                        "  margin:1em auto;"+Utils.LineSep +
                        "}"+Utils.LineSep +
                        ".printouttable th {"+Utils.LineSep +
                        "  text-align: center;"+Utils.LineSep +
                        "}"+Utils.LineSep +
                        ".printouttable td {"+Utils.LineSep +
                        "  border-color: #ddf #ddf #ddf #ddf;"+Utils.LineSep +
                        "  border-style: solid;"+Utils.LineSep +
                        "  border-width: 1px;"+Utils.LineSep +
                        "  padding:0.1em 1em 0.1em 0.5em;"+Utils.LineSep +
                        "}"+Utils.LineSep +
                        Utils.LineSep +
                        ".printouttable td:first-child {"+Utils.LineSep +
                        "  font-weight: 700;"+Utils.LineSep +
                        "  text-align: right;"+Utils.LineSep +
                        "  padding-right: 5px;"+Utils.LineSep +
                        "  padding-left: 5px;"+Utils.LineSep +
                        Utils.LineSep +
                        "}"+Utils.LineSep +
                        ".printouttable .printanswersquestion td{"+Utils.LineSep +
                        "  background-color:#F7F8FF;"+Utils.LineSep +
                        "}"+Utils.LineSep +
                        Utils.LineSep +
                        ".printouttable .printanswersquestionhead td{"+Utils.LineSep +
                        "  text-align: left;"+Utils.LineSep +
                        "  background-color:#ddf;"+Utils.LineSep +
                        "}"+Utils.LineSep +
                        Utils.LineSep +
                        ".printouttable .printanswersgroup td{"+Utils.LineSep +
                        "  text-align: center;"+Utils.LineSep +
                        "  font-weight:bold;"+Utils.LineSep +
                        "  padding-top:1em;"+Utils.LineSep +
                        "}"+Utils.LineSep +
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
                        "{ANSWERTABLE}"*/ ,
                "surveyls_numberformat", "0"};
        appendFieldsAndCDATARow(surveysLanguageSettings, surveysLanguageSettings_fields);
    }
            
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
    
    private int appendGroup(Questionnaire.GroupStarter question) {
        Node groups = findFirstNode("groups");
        
        int newGroup = ++groupId;
        appendCDATARow(findFirstChild(groups, "rows"), 
                new String[] {
                    "gid", Integer.toString(newGroup), 
                    "sid", Integer.toString(surveyId),
                    "group_name", "personal", 
                    "group_order", Integer.toString(groupOrder++), 
                    "description", question.groupMessage, 
                    "language", "en", 
                    "randomization_group", "", 
                    "grelevance", ""
                });
        
        return newGroup;
    }
    
    private void appendAuthorisationQuestion(Questionnaire.AuthQuestion question) {
        if (groupId!=0 || questionId!=0 || quotaId!=0)
            Utils.fail("can't add authorisation question after anything else: it must come as first question");
        
        Node groups = findFirstNode("groups");
        int authorisationGroup = ++groupId;
        
        appendCDATARow(findFirstChild(groups, "rows"), 
            new String[] {
                "gid", Integer.toString(authorisationGroup), 
                "sid", Integer.toString(surveyId),
                "group_name", question.id+"Group", 
                "group_order", Integer.toString(groupOrder++), 
                "description", "Accept questionnaire conditions", 
                "language", "en", 
                "randomization_group", "", 
                "grelevance", ""
            });
        
        final int qAuth = ++questionId;
        appendQuestionRow(
            new String[] {
                    "qid", Integer.toString(qAuth),
                    "parent_qid", "0",
                    "sid", Integer.toString(surveyId),
                    "gid", Integer.toString(authorisationGroup),
                    "type", "L",
                    "title", question.id,
                    "question", TextUtils.enParaHTML(question.authText)+
                                TextUtils.HTMLParaSep+
                                "<p>Please choose one of the following options:</p>",
                    "preg", "",
                    "help", "", 
                    "other", "N",
                    "mandatory", "Y",
                    "question_order", "0",
                    "language", "en",
                    "scale_id", "0",
                    "same_default", "0",
                    "relevance", "1"
                });
        
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
                "answer", question.OKtext,
                "sortorder", "1",
                "assessment_value", "0", 
                "language", "en",
                "scale_id", "0"
            });
        
        appendAnswerRow(
            new String[] {
                "qid", Integer.toString(qAuth),
                "code", "N",
                "answer", question.NotOKtext,
                "sortorder", "2",
                "assessment_value", "1", 
                "language", "en",
                "scale_id", "0"
            });
        
        final Node quota = findFirstNode("quota");
        final int quotaUnwilling = ++quotaId;
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

        final Node quotaMembers = findFirstNode("quota_members");
        final int quotaUnwillingMember = ++quotaMemberId; 
        
        appendCDATARow(findFirstChild(quotaMembers, "rows"),
            new String[] {
                "id", Integer.toString(quotaUnwillingMember),
                "sid", Integer.toString(surveyId),
                "qid", Integer.toString(qAuth),
                "quota_id", Integer.toString(quotaUnwilling),
                "code", "N"
            });
        
        final Node quotaLanguageSettings = findFirstNode("quota_languagesettings");
        final int quotaUnwillingLanguageSettings = ++quotaLanguageSettingsId;

        appendCDATARow(findFirstChild(quotaLanguageSettings, "rows"),
            new String[] {
                "quotals_id", Integer.toString(quotaUnwillingLanguageSettings),
                "quotals_quota_id", Integer.toString(quotaUnwilling),
                "quotals_language", "en",
                "quotals_name", "Unwilling",
                "quotals_message", TextUtils.enParaHTML(question.NotOKpara),
                "quotals_url", exitURL,
                "quotals_urldescrip", exitURLDescription
            });
    }
    
    private String compulsoryFlag(Questionnaire.Question q) {
        return q.compulsory ? "Y" : "N";
    }
    
    private String relevance(Pair<String,String> condition) {
        if (condition==null)
            return("1");
        // otherwise ...
        Pair<Integer,Integer> questionXgroup = questionMap.get(condition.getValue0());
            // can't return null, because ConditionalSection checks that.
        String conditionName = surveyId+"X"+
                                questionXgroup.getValue1().intValue()+"X"+
                                questionXgroup.getValue0().intValue();
        int cId = ++conditionId;
        appendConditionRow(
            new String[] {
                "cid", Integer.toString(cId),
                "qid", Integer.toString(questionId),    // i.e. the current question
                "scenario", "1",
                "cqid", questionXgroup.getValue0().toString(),
                "cfieldname", conditionName,
                "method", "==   ",
                "value", condition.getValue1()
            });
        return "("+conditionName+".NAOK == \""+condition.getValue1()+"\")";
    }
    public void appendTextQuestion(Questionnaire.TextQuestion question, Pair<String,String> condition) {
        int qId = ++questionId;
        appendQuestionRow(                
            new String[] {
                "qid", Integer.toString(qId),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(currentGroup),
                "type", "S",
                "title", question.id,
                "question", TextUtils.enParaHTML(question.questionText),
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", compulsoryFlag(question),
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", relevance(condition)
            });
        appendQuestionAttributesRows(Integer.toString(qId),
            new String[] {
                "hide_tip", "1",
                /*"location_mapheight", "300",
                "location_mapwidth", "500",
                "location_mapzoom", "11",*/
                "text_input_width", "50",
                "time_limit_action", "1"
            });
    }

    public void appendNumberQuestion(Questionnaire.NumberQuestion question, Pair<String,String> condition) {
        int qId = ++questionId;
        appendQuestionRow(
            new String[] {
                "qid", Integer.toString(qId),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(currentGroup),
                "type", "S",
                "title", question.id,
                "question", TextUtils.enParaHTML(question.questionText),
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", compulsoryFlag(question),
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", relevance(condition)
            });
        appendQuestionAttributesRows(Integer.toString(qId),
            new String[] {
                "hide_tip", "1",
                /*"location_mapheight", "300",
                "location_mapwidth", "500",
                "location_mapzoom", "11",*/
                "numbers_only", "1",
                "text_input_width", "50",
                "time_limit_action", "1"
            });
        
    }

    public void appendChoiceQuestion(Questionnaire.ChoiceQuestion question, Pair<String,String> condition) {
        int qId = ++questionId;
         appendQuestionRow(
             new String[] {
                 "qid", Integer.toString(qId),
                 "parent_qid", "0",
                 "sid", Integer.toString(surveyId),
                 "gid", Integer.toString(currentGroup),
                 "type", "L",
                 "title", question.id,
                 "question", TextUtils.enParaHTML(question.questionText),
                 "preg", "",
                 "help", "", 
                 "other", "N",
                 "mandatory", compulsoryFlag(question),
                 "question_order", Integer.toString(questionOrder++),
                 "language", "en",
                 "scale_id", "0",
                 "same_default", "0",
                 "relevance", relevance(condition)
             });
         appendQuestionAttributesRows(Integer.toString(qId),
             new String[] {
                 "hide_tip", "1",
                 "display_columns", Integer.toString(question.columns)
             });
         
         int sortorder = 0;
         for (Pair<String, String[]> p : question.options) 
             appendAnswerRow(
                 new String[] {
                     "qid", Integer.toString(qId),
                     "code", p.getValue0(),
                     "answer", TextUtils.enParaHTML(p.getValue1()),
                     "sortorder", Integer.toString(++sortorder),
                     "assessment_value", "1", 
                     "language", "en",
                     "scale_id", "0"
                 });
     }
     
    public void appendMultiChoiceQuestion(Questionnaire.MultiChoiceQuestion question, Pair<String,String> condition) {
        int qId = ++questionId;
         appendQuestionRow(
             new String[] {
                 "qid", Integer.toString(qId),
                 "parent_qid", "0",
                 "sid", Integer.toString(surveyId),
                 "gid", Integer.toString(currentGroup),
                 "type", "M",
                 "title", question.id,
                 "question", TextUtils.enParaHTML(question.questionText),
                 "preg", "",
                 "help", "", 
                 "other", "N",
                 "mandatory", compulsoryFlag(question),
                 "question_order", Integer.toString(questionOrder++),
                 "language", "en",
                 "scale_id", "0",
                 "same_default", "0",
                 "relevance", relevance(condition)
             });
         
         appendQuestionAttributesRows(
                 Integer.toString(qId),
                 new String[] {
                     "hide_tip", "1",
                     "display_columns", Integer.toString(question.columns),
                     "assessment_value", "1" /* ,
                     "other_replace_text", "Other", // should be parameterised
                     "other", "Y" */
                 });
         
         // options
         int subquestionOrder = 0;
         for (Pair<String, String[]> opt : question.options) 
         appendSubquestionRow(
                 new String[] {
                         "qid", Integer.toString(++questionId),
                         "parent_qid", Integer.toString(qId),
                         "sid", Integer.toString(surveyId),
                         "gid", Integer.toString(currentGroup),
                         "type", "T",
                         "title", opt.getValue0(),
                         "question", TextUtils.enLine(opt.getValue1()),
                         "preg", "",
                         "help", "", 
                         "other", "N",
                         "mandatory", "",
                         "question_order", Integer.toString(++subquestionOrder),
                         "language", "en",
                         "scale_id", "0",
                         "same_default", "0",
                         "relevance", ""
                     });

         // 'other' included in subquestions
         /* appendSubquestionRow(
                 new String[] {
                         "qid", Integer.toString(++questionId),
                         "parent_qid", Integer.toString(qId),
                         "sid", Integer.toString(surveyId),
                         "gid", Integer.toString(currentGroup),
                         "type", "T",
                         "title", "Other",
                         "question", "Other",
                         "preg", "",
                         "help", "", 
                         "other", "N",
                         "mandatory", "",
                         "question_order", Integer.toString(++subquestionOrder),
                         "language", "en",
                         "scale_id", "0",
                         "same_default", "0",
                         "relevance", ""
                     }); */
     }
     
    public void appendArrayChoiceQuestion(Questionnaire.ArrayChoiceQuestion question, Pair<String,String> condition) {
        int qId = ++questionId;
         appendQuestionRow(
             new String[] {
                 "qid", Integer.toString(qId),
                 "parent_qid", "0",
                 "sid", Integer.toString(surveyId),
                 "gid", Integer.toString(currentGroup),
                 "type", "F",
                 "title", question.id,
                 "question", TextUtils.enParaHTML(question.questionText),
                 "preg", "",
                 "help", "", 
                 "other", "N",
                 "mandatory", compulsoryFlag(question),
                 "question_order", Integer.toString(questionOrder++),
                 "language", "en",
                 "scale_id", "0",
                 "same_default", "0",
                 "relevance", relevance(condition)
             });
         
         appendQuestionAttributesRows(Integer.toString(qId),
             new String[] {
                 "hide_tip", "1"
             });
         
         // horizontals
         int sortorder = 0;
         for (Pair<String,String[]> v : question.vertics)
             appendAnswerRow(
                     new String[] {
                     "qid", Integer.toString(qId),
                     "code", v.getValue0(),
                     "answer", TextUtils.enLine(v.getValue1()),
                     "sortorder", Integer.toString(++sortorder),
                     "assessment_value", "1", 
                     "language", "en",
                     "scale_id", "0"
             });
         
         for (Pair<String, String[]> h : question.horizs) 
             appendSubquestionRow(
                 new String[] {
                         "qid", Integer.toString(++questionId),
                         "parent_qid", Integer.toString(qId),
                         "sid", Integer.toString(surveyId),
                         "gid", Integer.toString(currentGroup),
                         "type", "T",
                         "title", h.getValue0(),
                         "question", TextUtils.enLine(h.getValue1()),
                         "preg", "",
                         "help", "", 
                         "other", "N",
                         "mandatory", "",
                         "question_order", Integer.toString(++subquestionOrder),
                         "language", "en",
                         "scale_id", "0",
                         "same_default", "0",
                         "relevance", ""
                     });
     }
     
    public void appendMultiTextQuestion(Questionnaire.MultiTextQuestion question, Pair<String,String> condition) {
        int qId = ++questionId;
        appendQuestionRow(
            new String[] {
                "qid", Integer.toString(qId),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(currentGroup),
                "type", "Q",
                "title", question.id,
                "question", TextUtils.enParaHTML(question.questionText),
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", compulsoryFlag(question),
                "question_order", Integer.toString(questionOrder++),
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", relevance(condition)
            });
        appendQuestionAttributesRows(Integer.toString(qId),
            new String[] { "assessment_value", "1" });
        
       subquestionOrder = 0;
       for (Pair<String, String[]> opt : question.options) 
           appendSubquestionRow(
               new String[] {
                   "qid", Integer.toString(++questionId),
                   "parent_qid", Integer.toString(qId),
                   "sid", Integer.toString(surveyId),
                   "gid", Integer.toString(currentGroup),
                   "type", "T",
                   "title", opt.getValue0(),
                   "question", TextUtils.enParaHTML(opt.getValue1()),
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
    }
    
    private void processConditionalSection(Questionnaire.ConditionalSection section, Pair<String,String> condition) {
        if (condition!=null)
            Utils.fail("Cannot handle nested conditionals (**If inside **If");
        else {
            if (questionMap.get(section.condition.getValue0())==null)
                Utils.fail("Error in **If "+section.condition.getValue0()+" "+section.condition.getValue1()+
                        ": "+section.condition.getValue0()+" is not a preceding question identifier");
            processQuestions(section.thens, section.condition);
            if (section.elses!=null)
                Utils.fail("Cannot handle **Else yet");
        }
    }
    
    private void processTestSection(Questionnaire.TestSection section, Pair<String,String> condition) {
        Test test = section.test;
        TestQuestion[] questions = test.progQuestions;
        AnswerPage[] answerPages = test.answerPages;
        int [] questionPageNumbers = test.questionPageNumbers;
        int nQuestionPages = test.nQuestionPages;
        int qlength = questions.length;
        
        if (condition!=null)
            Utils.fail("Cannot handle conditional tests yet");
        
        Node groups = findFirstNode("groups");
        int programGroups[] = new int[nQuestionPages];
        for (int i=0; i<nQuestionPages; i++) {
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
        
        for (int qidx = 0; qidx<qlength; qidx++)
            appendProgramsQuestion(Utils.ordinal(qidx), questions[qidx], answerPages[qidx], 
                    programGroups[questionPageNumbers[qidx]]);  
    }

    public void processFinalQuestion(Questionnaire.FinalQuestion question, Pair<String,String> condition) {
        if (condition!=null)
            Utils.fail("**Final cannot occur in a conditional section");
        if (finalised)
            Utils.fail("two **Final entries");
        finalised = true;
        
        // the finish group
        int gId = ++groupId;
        appendCDATARow(findFirstChild(findFirstNode("groups"), "rows"), 
            new String[] {
                "gid", Integer.toString(gId), 
                "sid", Integer.toString(surveyId),
                "group_name", question.id+"Group", 
                "group_order", Integer.toString(groupOrder++), 
                "description", "", 
                "language", "en", 
                "randomization_group", "", 
                "grelevance", ""
            });

        int qId = ++questionId;
        appendQuestionRow(
            new String[] {
                "qid", Integer.toString(qId),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(gId),
                "type", "U",
                "title", question.id,
                "question", 
                    TextUtils.enParaHTML(
                        Utils.concatArrays(
                            question.finalText,
                            new String[] {
                                "If you've finished, press the &quot;Submit&quot; button to record your answers.",
                                "If you want to go back to your answers, use the &quot;Previous&quot; button.",
                                "If you want to delete all your answers, press &quot;Exit and clear survey&quot;."
                            }
                        )
                    ),
                "preg", "",
                "help", "", 
                "other", "N",
                "mandatory", "N",
                "question_order", "0",
                "language", "en",
                "scale_id", "0",
                "same_default", "0",
                "relevance", "1"
            });
       
    }
    
    public void appendProgramsQuestion(int qnum /* ordinal not idx */, TestQuestion q, AnswerPage ap, int progGroupId) {
        Node questions = findFirstChild(doc.getDocumentElement(), "questions");
        Node qrows = findFirstChild(questions, "rows");
        
        String[] qlines = PaperQuestionnaire.questionLines(q);
        for (int i=0; i<qlines.length; i++)
            if (qlines[i].equals(""))
                qlines[i] = "<div>"+TextUtils.LineSep+TextUtils.LineSep+"&nbsp;</div>"+TextUtils.LineSep;
            else
                qlines[i] = "<div>"+TextUtils.LineSep+TextUtils.LineSep+qlines[i]+"</div>"+TextUtils.LineSep;
        
        int qId = ++questionId;
        
        appendCDATARow(qrows,
                new String[] {
                "qid", Integer.toString(qId),
                "parent_qid", "0",
                "sid", Integer.toString(surveyId),
                "gid", Integer.toString(progGroupId),
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
        
        appendQuestionAttributesRows(arows, 
                Integer.toString(qId),
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
            int sqId = ++questionId;
            String sqidString = Integer.toString(i+1);
            /* System.out.println("subquestion "+sqidString);
            System.out.flush();
            System.out.println(Generator.stringOfStatePairs(cs.item(i)));
            System.out.flush(); */
            appendCDATARow(sqrows,
                    new String[] {
                    "qid", Integer.toString(sqId),
                    "parent_qid", Integer.toString(qId),
                    "sid", Integer.toString(surveyId),
                    "gid", Integer.toString(progGroupId),
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
    
    private void processQuestions(Questionnaire.Question questions[], Pair<String,String> condition) {
        for (Questionnaire.Question question : questions) {
            if (question!=null) {
                if (finalised)
                    Utils.fail("no questions can follow **Final -- LimeSurveyQuestionnaire.processQuestions sees "+question);
                if (!question.id.equals(""))
                    questionMap.put(question.id, Pair.with(new Integer(questionId+1), // because we use ++questionId
                                                            new Integer(currentGroup)));
                
                if (question instanceof Questionnaire.AuthQuestion)
                    appendAuthorisationQuestion((Questionnaire.AuthQuestion)question);
                else
                if (question instanceof Questionnaire.GroupStarter)
                    currentGroup = appendGroup((Questionnaire.GroupStarter)question);
                else
                if (question instanceof Questionnaire.TextQuestion)
                    appendTextQuestion((Questionnaire.TextQuestion)question, condition);
                else
                if (question instanceof Questionnaire.MultiTextQuestion)
                    appendMultiTextQuestion((Questionnaire.MultiTextQuestion)question, condition);
                else
                if (question instanceof Questionnaire.NumberQuestion)
                    appendNumberQuestion((Questionnaire.NumberQuestion)question, condition);
                else
                if (question instanceof Questionnaire.ChoiceQuestion)
                    appendChoiceQuestion((Questionnaire.ChoiceQuestion)question, condition);
                else
                if (question instanceof Questionnaire.MultiChoiceQuestion)
                    appendMultiChoiceQuestion((Questionnaire.MultiChoiceQuestion)question, condition);
                else
                if (question instanceof Questionnaire.ArrayChoiceQuestion)
                    appendArrayChoiceQuestion((Questionnaire.ArrayChoiceQuestion)question, condition);
                else
                if (question instanceof Questionnaire.ConditionalSection)
                    processConditionalSection((Questionnaire.ConditionalSection)question, condition);
                else
                if (question instanceof Questionnaire.TestSection)
                    processTestSection((Questionnaire.TestSection)question, condition);
                else
                if (question instanceof Questionnaire.FinalQuestion)
                    processFinalQuestion((Questionnaire.FinalQuestion)question, condition);
                else
                    Utils.fail("LimeSurveyQuestionnaire.processQuestions cannot handle "+question);
            }
        }
    }
 
 /*       // the programs group of questions
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
}*/
        
    // utility methods
    public Node findFirstChild(Node n, String s) {
        NodeList ns = n.getChildNodes();
        for (int i=0; i<ns.getLength(); i++)
            if (ns.item(i).getNodeName().equals(s))
                return ns.item(i);
        Utils.fail("Node "+n.getNodeName()+" doesn't have a "+s+" child");
        return null; // can't happen
    }
    
    public Node findFirstChild(Element e, String s) {
        if (!(e instanceof Node))
            Utils.fail("findFirstChild given non-Node "+e);
        return findFirstChild((Node) e, s);
    }
    
    public Node findFirstNode(String nodename) {
        return findFirstChild(doc.getDocumentElement(), nodename);
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
            Utils.crash("TransformerException "+tfe+" while printing LimeSurvey questionnaire");
        }
    }
    
    @SuppressWarnings("serial")
    static class Headers extends SpreadsheetHeaders {
        int tiStart=0;
        
        Headers(int size) {
            super(size);
        }

        void addColumns(Test qu) {
            quStart = this.size();
            for (int i = 0; i<qu.progQuestions.length; i++) {
                String qid = Integer.toString(Utils.ordinal(i));
                for (int j = 0; j<qu.answerPages[i].choices.size(); j++)
                    addColumn(qid, Integer.toString(Utils.ordinal(j)));
                addColumn(qid, "other");
            }
            quEnd = this.size();
        }

        void startTimingHeaders() {
            tiStart = this.size();
        }
        void addTimingHeaders(Questionnaire.Question[] qs) {
            if (qs!=null)
                for (Questionnaire.Question q : qs)
                    if (q instanceof Questionnaire.AuthQuestion ||
                        q instanceof Questionnaire.FinalQuestion) {
                        // is its own group
                        addGroupTimingHeader();
                        addTimingHeader(q.id);
                    }
                    else
                    if (q instanceof Questionnaire.ChoiceQuestion || 
                        q instanceof Questionnaire.TextQuestion || 
                        q instanceof Questionnaire.NumberQuestion ||
                        q instanceof Questionnaire.MultiTextQuestion ||
                        q instanceof Questionnaire.MultiChoiceQuestion||
                        q instanceof Questionnaire.ArrayChoiceQuestion)
                        addTimingHeader(q.id);
                    else
                    if (q instanceof Questionnaire.TestSection)
                        addTimingHeaders(((Questionnaire.TestSection)q).test);
                    else
                    if (q instanceof Questionnaire.ConditionalSection) {
                        addTimingHeaders(((Questionnaire.ConditionalSection)q).thens);
                        addTimingHeaders(((Questionnaire.ConditionalSection)q).elses);
                    }
                    else
                    if (q instanceof Questionnaire.GroupStarter)
                        addGroupTimingHeader();
                    else
                        Utils.crash("Generator.addTimingHeaders cannot handle "+q);
        }
        
        void addGroupTimingHeader() {
            this.add("groupTime");
        }
        
        void addTimingHeaders(Test qu) {
            int pageNum = -1;
            for (int i = 0; i<qu.progQuestions.length; i++) {
                if (qu.questionPageNumbers[i]!=pageNum) {
                    addGroupTimingHeader();
                    pageNum = qu.questionPageNumbers[i];
                }
                addTimingHeader(Integer.toString(Utils.ordinal(i)));
            }
        }
        
        void addTimingHeader(String qid) {
            this.add(qid+"Time");
        }
        
        // no states, no Q, no colons in question headers.
        @Override
        void addTestSubQuestionColumn(int qidx, int subidx, State b) {
            addColumn(testSubQuestionHeaderLS(qidx, subidx));
        }
        
        @Override
       void addTestOtherColumn(int qidx) {
           addColumn(testOtherQuestionHeaderLS(qidx));
        }

        static String testSubQuestionHeaderLS(int qidx, int subidx) {
            return "\""+Utils.ordinal(qidx)+" ["+Utils.ordinal(subidx)+"]\"";
        }
        
        static String testOtherQuestionHeaderLS(int qidx) {
            return "\""+Utils.ordinal(qidx)+" [Other]\"";
        }

    }
    

}
