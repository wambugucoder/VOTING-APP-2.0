import React from 'react';
import './App.css';
import store from './redux/store';
import { Provider } from "react-redux";
import Landing from './components/Landing';
import Register from './components/auth/Register';
import Login from './components/auth/Login';
import CreatePoll from './components/polls/CreatePoll';
import Vote from './components/polls/Vote';
import { Route, BrowserRouter as Router, Switch }  from 'react-router-dom';
import 'react-notifications/lib/notifications.css';
import Verify from './components/token/Verify'
import dashboard from './components/dashboard/dashboard';
import 'bootstrap/dist/css/bootstrap.min.css';
import setAuthToken from './utils/setAuthToken'
import jwt_decode from 'jwt-decode';
import { LOGIN_USER, LOGOUT_USER } from './redux/action/types';
import 'react-notifications/lib/notifications.css';
 
//SESSION MANAGEMENT IN MAIN APP
if(localStorage.jwtToken){
const token=localStorage.jwtToken
//set header
setAuthToken(token)
//decode
const decoded=jwt_decode(token)
//call store to keep user authenticated and in session
store.dispatch({
  type:LOGIN_USER,
  payload:decoded
})


 // Check for expired token
 const currentTime= Date.now()/1000;
 if (decoded.exp < currentTime) {
   // Logout user
  store.dispatch({
     type:LOGOUT_USER,
     
   });
   // Redirect to login
   window.location.href = "/login";
 }
}
function App() {
  return (
    
     <Provider store={store}>
     <Router>
     <div className="App">
     <Route exact path='/' component={Landing}/>
     <Route exact path='/register' component={Register}/>
     <Route exact path='/login' component={Login}/>
     <Route exact path='/verify/:token' component={Verify}/>
     
     <Route exact path='/create-poll' component={CreatePoll}/>
     <Route exact path='/vote/:id' component={Vote}/>
     
    
     <Switch>
     <Route exact path='/dashboard' component={dashboard}/>
     </Switch>
     

     </div>
     </Router>
    
     </Provider>
    
  );
}

export default App;
