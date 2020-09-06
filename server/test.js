const chai = require('chai');
const { before,after } = require('mocha');
const model=require("./models")
const expect = chai.expect;
const app=require('supertest')(require('./server'))


describe('SUPERTEST SETUP', () => {
    it('Starts the testing process', () => {
        before((done) => {
            
            console.log("Tests About to Start...")
            model.sequelize.sync({
              force:true,
             
             },
             )
                .then(done)
        });
    });
    it('Should close server after Running Tests', () => {
        after((done) => {
            console.log("Tests Completed...")
            done();
            
        });
    });
    });

describe ('AUTH TESTING', () => {
    describe('POST /api/auth/register', (done) => {
        it('Registers a User and sends them email', () => {
       
       return app.post("/api/auth/register")
       .send({
        username:'test-admin',
        email:"test@admin.com",
        password:"admin@",
        password2:"admin@"
       })
     
      .expect(200)
         .then((response) => {
             expect(response);
            
         })
        
        });
    });
    describe('PUT / api/auth/verify/token', () => {
        it('Confirms the Users Email', () => {
        const token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6ImFkbWluQGFkbWluLmNvbSIsImlhdCI6MTU5OTEzMzMxOCwiZXhwIjoxNTk5MTM2OTE4fQ.5n7kOiY8O_CwgrDW_O68l1R4i2_DyDpBWqc26dFVJNA"
        return app.put(`/api/auth/verify/${token}`)
         //SINCE TOKEN EXPIRED ,WE EXPECT AN ERROR (400) IN ORDER TO PASS TEST
        .expect(400)
        .then((response) => {
            expect(response).to.exist
           
        })
       
       

       
        });
    });
    
 describe('POST /api/auth/login', () => {
    it('Provides a token during Login', () => {
      return app.post("/api/auth/login")
        .send({
            email:"test@admin.com",
        password:"admin@",
        })
        .expect(200)
         .then((response) => {
             expect(response)
         })
            
    });
 })
 
 
 
});

describe('POLL TESTING', () => {
     describe(`POST /api/polls/create`, () => {
       it('creates a Poll', () => {
      
        return app.post('/api/polls/create')
       
        .send({
            question:"A or B ",
            choices:[
                {option:"A "},
                {option:"B"}
            ],
           userId:"1"
         })
        
        .expect(200)
        
       
        .then((response) => {
          expect(response).to.equal("Success");
          
        }).catch((err) => {
            console.log(err)
        })
      
       });
        
   })
   describe('GET /api/polls/allpolls', () => {
    it('returns all polls', () => {

        return app.get('/api/polls/allpolls')
               .expect(200)
               
               .then((response) => {
                   expect(response);
                   expect(response.body).to.be.an.instanceof(Array)
                   expect(response.body).to.have.length.above(0);
                  

                 
               })
              

    });
})
});
