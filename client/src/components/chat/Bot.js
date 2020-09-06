import React, { Component } from 'react';
import ChatBot from 'react-simple-chatbot';
import { ThemeProvider } from 'styled-components';
class Bot extends Component {
 

  render() {
    const theme = {
        background: '#f5f8fb',
        fontFamily: 'Roboto',
        headerBgColor: '#0e97b3',
        headerFontColor: '#fff',
        headerFontSize: '15px',
        botBubbleColor: '#0e97b3',
        botFontColor: '#fff',
        userBubbleColor: '#fff',
        userFontColor: '#4a4a4a',
      };
      const steps=[
        {
          id: '1',
          message: 'Hello,would you like to know how the App functions?',
          trigger: '2',
        },
        {
          id: '2',
          options: [
            { value: 1, label: 'Yes', trigger: '3' },
            { value: 2, label: 'No', trigger: '4' },
           
          ],
        },
        {
          id: '3',
          message: 'A polling App helps us collect Data',
          trigger: '6',
        },
        {
        id: '6',
        message: 'There are two categories of people',
        trigger: '7',
      },
      {
      id: '7',
      options: [
        { value: 1, label: 'Users', trigger: '8' },
        { value: 2, label: 'Admin', trigger: '9' },
        { value: 3, label: 'one', trigger: '10' },
      ],
    }, 
    {
        id: '8',
        message: 'Users have the privilege of voting and can only vote once in a poll',
        trigger: '7',
      },
      {
        id: '9',
        message: 'Admins have the privilege of creating polls,closing them and also voting',
        trigger: '7',
      },
      {
        id: '10',
        message: 'Do you now have an idea of how the App functions ',
       trigger:'20',
      },
      {
        id: '20',
        options: [
          { value: 1, label: 'Yes', trigger: '11' },
          { value: 2, label: 'No', trigger: '12' },
         
        ],
      }, 
      {
        id: '12',
        message: 'Im sorry we couldnt help you enough',
       trigger:'13',
      
      },
      {
        id: '13',
        message: 'Please Contact us at abc@polls.com for further assistance',
       trigger:'4',
       
      },
      {
        id: '11',
        message: 'We are glad we could be of Assistance',
       trigger:'4',
       
      },
        {

          id: '4',
          message: 'Thank you for your time',
        
         end:true
        },
       
      ]
    return (
      <div align="center">
     <ThemeProvider theme={theme}>
    <ChatBot steps={steps}
     headerTitle="Poll Support"
     customDelay="500"
                 
                  floating="true"
                  width="700px"
     />;
  </ThemeProvider>
      </div>
    );
  }
}

export default Bot;