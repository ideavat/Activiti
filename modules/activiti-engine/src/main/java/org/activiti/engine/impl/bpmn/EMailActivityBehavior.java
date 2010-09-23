/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.bpmn;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.bpmn.BpmnJavaDelegation;
import org.activiti.engine.impl.cfg.ProcessEngineConfiguration;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.pvm.delegate.DelegateExecution;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;


/**
 * @author Joram Barrez
 */
public class EmailActivityBehavior extends BpmnJavaDelegation {

  private String to;
  private String from;
  private String cc;
  private String bcc;
  private String subject;
  private String text;
  private String html;
  
  public void execute(DelegateExecution execution) {
    Email email = createEmail();
    
    addTo(email);
    setFrom(email);
    addCc(email);
    addBcc(email);
    setSubject(email);
    setMailServerProperties(email);
    
    try {
      email.send();
    } catch (EmailException e) {
      throw new ActivitiException("Could not send e-mail", e);
    }
  }
  
  protected Email createEmail() {
    if (html != null) {
      return createHtmlEmail();
    } else if (text != null) {
      return createTextOnlyEmail();
    } else {
      throw new ActivitiException("'html' or 'text' is required to be defined when using the mail activity");
    }
  }
  
  protected HtmlEmail createHtmlEmail() {
    HtmlEmail email = new HtmlEmail();
    try {
      email.setHtmlMsg(html);
      if (text != null) { // for email clients that don't support html
        email.setTextMsg(text);
      }
      return email;
    } catch (EmailException e) {
      throw new ActivitiException("Could not create HTML email", e);
    }
  }
  
  protected SimpleEmail createTextOnlyEmail() {
    SimpleEmail email = new SimpleEmail();
    try {
      email.setMsg(text);
      return email;
    } catch (EmailException e) {
      throw new ActivitiException("Could not create text-only email", e);
    }
  }
  
  protected void addTo(Email email) {
    String[] tos = splitAndTrim(to);
    if (tos != null) {
      for (String t : tos) {
        try {
          email.addTo(t);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + t + " as recipient", e);
        }
      }
    } else {
      throw new ActivitiException("No recipient could be found for sending email");
    }
  }
  
  protected void setFrom(Email email) {
    String fromAddres = null;
    
    if (this.from != null) {
      fromAddres = from;
    } else { // use default configured from address in process engine config
      fromAddres = CommandContext.getCurrent().getProcessEngineConfiguration().getMailServerDefaultFrom();
    }
    
    try {
      email.setFrom(fromAddres);
    } catch (EmailException e) {
      throw new ActivitiException("Could not set " + from + " as from address in email", e);
    }
  }
  
  protected void addCc(Email email) {
    String[] ccs = splitAndTrim(cc);
    if (ccs != null) {
      for (String c : ccs) {
        try {
          email.addCc(c);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + c + " as cc recipient", e);
        }
      }
    }
  }
  
  protected void addBcc(Email email) {
    String[] bccs = splitAndTrim(bcc);
    if (bccs != null) {
      for (String b : bccs) {
        try {
          email.addBcc(b);
        } catch (EmailException e) {
          throw new ActivitiException("Could not add " + b+ " as bcc recipient", e);
        }
      }
    }
  }
  
  protected void setSubject(Email email) {
    email.setSubject(subject != null ? subject : "");
  }
  
  protected void setMailServerProperties(Email email) {
    ProcessEngineConfiguration config = CommandContext.getCurrent().getProcessEngineConfiguration();
    
    String host = config.getMailServerSmtpHost();
    if (host == null) {
      throw new ActivitiException("Could not send email: no SMTP host is configured");
    }
    email.setHostName(host);
    
    int port = config.getMailServerSmtpPort();
    email.setSmtpPort(port);
    
    String user = config.getMailServerSmtpUserName();
    String password = config.getMailServerSmtpPassword();
    if (user != null && password != null) {
      email.setAuthentication(user, password);
    }
  }
  
  protected String[] splitAndTrim(String str) {
    if (str != null) {
      String[] splittedStrings = str.split(",");
      for (int i=0; i<splittedStrings.length; i++) {
        splittedStrings[i] = splittedStrings[i].trim();
      }
      return splittedStrings;
    }
    return null;
  }
  
}