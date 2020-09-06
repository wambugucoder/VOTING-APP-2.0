import { GET_ALL_POLLS , IS_LOADING, CREATE_POLL, VOTE_POLL, GET_BY_ID, DELETE_BY_ID, CLOSE_POLL} from "../action/types";

const INITIAL_STATE = {
    isRetrieved:false,
    isLoading:false,
    isCreated:false,
    hasVoted:false,
    isRetrievedById:false,
    isDeleted:false,
    isClosed:false,
    polls:[]
};
 
export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case GET_ALL_POLLS:
            return {...state,
            isRetrieved:true,
            isLoading:false,
            polls:action.payload
        }
        case CREATE_POLL:
            return {...state,
           isCreated:true,
            isLoading:false,
           
        }
        case VOTE_POLL:
            return {...state,
          
           hasVoted:true
            
        }
        case GET_BY_ID:
            return {...state,
          
          isRetrievedById:true,
          polls:action.payload
            
        }
        case IS_LOADING:
            return {...state,
          
            isLoading:true,
            
        }
        case DELETE_BY_ID:
            return {...state,
          
           isDeleted:true,
            
        }
        case CLOSE_POLL:
          return{
              ...state,
              isClosed:true
          }
          
        default:
            return state
    }
}