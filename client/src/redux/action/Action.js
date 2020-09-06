import axios from "axios";
import jwt_decode from 'jwt-decode';

import { REGISTER_USER ,IS_LOADING, GET_ERRORS, LOGIN_USER, VERIFY_USER, GET_ALL_POLLS, CREATE_POLL, VOTE_POLL, GET_BY_ID, DELETE_BY_ID, CLOSE_POLL,LOGOUT_USER } from "./types";
import setAuthToken from '../../utils/setAuthToken'


//ACTION TO TRANSPORT DATA TO BACKEND

export const RegisterUser = (UserData) => dispatch => {
  dispatch({
      type:IS_LOADING
      
    })
axios.post("/api/auth/register",UserData).then(res =>
  
  dispatch({
    type:REGISTER_USER ,
    payload: res.data
  })
  
).catch((errors) => {
  dispatch({
      type:GET_ERRORS ,
      payload: errors.response.data
    }) 
});
};

//TRANSPORT LOGIN DATA
export const LoginUser = (UserData) => dispatch => {
  dispatch({
      type:IS_LOADING
      
    })
axios.post("/api/auth/login",UserData).then(res =>{
     // Save to localStorage
      // Set token to localStorage
      const {token}=res.data;
      localStorage.setItem("jwtToken",token);
       // Set token to Auth header
       setAuthToken(token)
        // Decode token to get user data
        const decoded=jwt_decode(token);
  dispatch({
    type:LOGIN_USER ,
    payload: decoded
  })
}).catch((err) => {
  dispatch({
      type:GET_ERRORS ,
      payload: err.response.data
    }) 
});
};
//TRANSPORT VERIFICATION TOKEN
export const VerifyUser = (token) => dispatch => {
  dispatch({
    type:IS_LOADING
    
  })
  axios.put(`/api/auth/verify/${token}`).then(res =>
    dispatch({
      type: VERIFY_USER,
      payload: res.data
    })
  ).catch((errors) => {
    dispatch({
      type:GET_ERRORS ,
      payload:errors.response.data
    }) 
 
});
};
//GET ALL POLLS
export const getAllPolls = () => dispatch => {
  dispatch({
    type:IS_LOADING
    
  })
  axios.get("/api/polls/allpolls").then(res =>
    dispatch({
      type:GET_ALL_POLLS ,
      payload: res.data
    })
  );
};
//TRANSPORT CREATED POLL
export const MakePoll = (PollData) => dispatch => {
   dispatch({
    type:IS_LOADING
    
  })
  axios.post("/api/polls/create",PollData).then(res =>
    dispatch({
      type:CREATE_POLL,
     
    })
  ).catch((errors) => {
    dispatch({
      type:GET_ERRORS ,
      payload:errors.response.data
    }) 
 
});
  
};

//GET BY ID ACTION
export const GetSpecificPoll = (id) => dispatch => {
  dispatch({
    type:IS_LOADING
    
  })
  axios.get(`/api/polls/${id}`).then(res =>
    dispatch({
      type:GET_BY_ID ,
      payload: res.data
    })
  );
};

//VOTING ACTION
export const VotePoll = (pid,uid,cid) => dispatch => {
  dispatch({
    type:IS_LOADING
    
  })
  axios.put(`/api/polls/${pid}/${uid}/${cid}`).then(res =>
    dispatch({
      type:VOTE_POLL 
     
    })
  ).catch((errors) => {
    dispatch({
      type:GET_ERRORS ,
      payload:errors.response.data
    }) ;
  });
};
export const DeletePoll = (id) => dispatch => {
  dispatch({
    type:IS_LOADING
    
  })
  axios.delete(`/api/polls/${id}`).then(res =>
    dispatch({
      type:DELETE_BY_ID ,
     
    })
  );
};
export const ClosePoll = (id) => dispatch => {
  dispatch({
    type:IS_LOADING
    
  })
  axios.put(`/api/polls/${id}`).then(res =>
    dispatch({
      type: CLOSE_POLL,
     
    })
  );
};
//LOGOUT USER
export const LogOutUser = () => dispatch => {
  // Remove token from local storage
 localStorage.removeItem("jwtToken");
 // Remove auth header for future requests
 setAuthToken(false);
  dispatch({
   type:LOGOUT_USER ,
   
 })
   
 };