import {GET_ERRORS,IS_LOADING} from '../action/types'
const INITIAL_STATE = {
    isLoading:false
};
 
export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case GET_ERRORS:
            return action.payload 
            case IS_LOADING:
                return {...state,
                    isRegistered:false,
                    isLoading:true
                }
        default:
            return state
    }
   
}