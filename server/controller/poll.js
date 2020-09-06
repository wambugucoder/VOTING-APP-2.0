const express = require('express');
const models=require("../models");
const {Op}=require('sequelize')
const router = express.Router();

/*
    @route  api/polls/allpolls
    @desc  Retrieve all polls and order them from highest date to lowest
    @access private(Only authenticated users)
*/
router.get('/allpolls', (req, res) => {
  models.Poll.findAll({
     //step 1
order:[
      ['createdAt','DESC']
],
     //step 2
    include:[
        {
    model:models.User,
    as:"author",
    attributes:['username']
        }
    ]

      
   })
      .then( poll=> {
        res.json(poll)
      })
});
/*
    @route  api/polls/create
    @desc Create a poll with choices association
    @access authenticated people
*/
router.post('/create', (req, res) => {
      models.Poll.create(req.body,{
          include:[{
              model:models.Choice,
              
              as:"choices"
          }]
       })
          .then(success=> {
            res.json("Success")
          })
});
/*
    @route  api/polls/id
    @desc GET Poll By Id
    @access Authenticated people
*/
router.get('/:id', (req, res) => {
  models.Poll.findOne({
     //step 1

    where:{id:req.params.id},
    attributes:{ exclude:['createdAt','updatedAt']},
    //step2
      include:[{
          model:models.Choice,
          as:"choices",
         attributes:{ exclude:['pollId','createdAt','updatedAt']}
      }]
   })
      .then(retrieved => {
        res.json(retrieved)
      })
});
/*
    @route  api/polls/delete
    @desc Delete A poll and its associated contents by Id
    @access Authenticated Persons
*/
router.delete('/:id', (req, res) => {
  models.Poll.destroy({
    //step 1
      where:{
          id:req.params.id
      }
     
   })
      .then(jobdone => {
        res.json("Deleted Successfully")
      })
});
/*
    @route  api/polls/:uId/:pId:/cId
    uId->Check if user if already exists n the voters registry
    @desc Facilitates the voting the voting process
    @access private
*/
router.put('/:pid/:uid/:cid', (req, res) => {
//step 1->Find the poll Id and check whethter the user has voted already
  models.Poll.findOne({
    where:{
      id:req.params.pid,
      voters:{[Op.contains]:[req.params.uid]}
    }

   })
      .then(step1 => {
        if (step1) {
         return res.status(400).json({ error: 'OOps you already voted' });
        }
        else{
          //step 2->Find the choice selected
           models.Choice.findOne({
             where:{
               id:req.params.cid,
               pollId:req.params.pid
            }
             
            })
            
               .then(step2 => {
                
               step2.increment('votes')
               })
               .then(step3 => {
                //step 3 ->Register the user in the voter registry to prevent double voting
                  models.Poll.findOne({
                    where:{id:req.params.pid}
                   })
                      .then(finalstep => {
                        finalstep.voters.push(req.params.uid)
                          models.Poll.update({
                            voters:finalstep.voters
                          },{
                            where:{id:req.params.pid}
                           })
                              .then(doneandcomplete=> {
                                res.json("You have successfully voted")
                              })
                      })
                })
        }
      })



});
//CLOSE POLL
router.put('/:id', (req, res) => {
  models.Poll.update(
    {active:false },
   {where:{id:req.params.id}}
   )
      .then(updated => {
        res.json({ message: 'Poll has been closed' });
      })
});



module.exports = router;