import React, { Component } from 'react';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Carousel from 'react-bootstrap/Carousel';

import NavBarMain from "./NavBarMain"
import Bot from './chat/Bot';


class Landing extends Component {
  
    render() {
   return (
     <Container fluid>
       <Row >
      
 <NavBarMain/>

         </Row>
       
        <Bot/>
        <Carousel>
  <Carousel.Item>
    <img
      className="d-block w-100"
      src="https://www.swissinfo.ch/resource/image/43503690/landscape_ratio3x2/580/386/a7b1644e92994da988fa993b2ae9e70f/MF/67176701_m-jpg.jpg"
     
      alt="First slide"
      
    />
    
  </Carousel.Item>
  <Carousel.Item>
    <img
      className="d-block w-100"
      src="https://thumbs.dreamstime.com/b/electronic-voting-concept-politics-elections-illustration-73463740.jpg"
      
      alt="Third slide"
      
    />

    
  </Carousel.Item>
 
</Carousel>

  
       
     </Container>
    );
  }
}

export default Landing;

