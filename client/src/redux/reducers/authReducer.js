import { REGISTER_USER, IS_LOADING, LOGIN_USER, VERIFY_USER, LOGOUT_USER } from "../action/types";

const INITIAL_STATE = {
    isLoading:false,
    isRegistered:false,
    isAuthenticated:false,
    isVerified:false,
    
    user:{},


};
 
export default (state = INITIAL_STATE, action) => {
    switch (action.type) {
        case REGISTER_USER:
            return {...state,
                isRegistered:true,
                isLoading:false,
                payload:action.payload
            }
            case IS_LOADING:
                return {...state,
                    isRegistered:false,
                    isLoading:true
                }
                case LOGIN_USER:
                    return {...state,
                       isAuthenticated:true,
                        isLoading:false,
                        user:action.payload
                    }
                    case VERIFY_USER:
                        return {...state,
                           isVerified:true,
                           isLoading:false,
                        }
                        case LOGOUT_USER:
                            return {...state,
                           isAuthenticated:false,
                           user:{}
                            }
        default:
            return state
    }
}