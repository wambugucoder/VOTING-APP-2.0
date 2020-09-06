'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class Poll extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
      //A.User ->Polls(1:m)
      this.belongsTo(models.User,{
        foreignKey:'userId',
        as:'author',
       
        
      })
      //B..Polls-> Choices(1:m)
 this.hasMany(models.Choice,{
foreignKey:'pollId',
as:'choices',
onDelete:'CASCADE',
onUpdate:'CASCADE',



 })
    }
  };
  Poll.init({
    question: DataTypes.STRING,
    voters: DataTypes.ARRAY(DataTypes.STRING),
    userId: DataTypes.INTEGER,
    active:DataTypes.BOOLEAN,
  }, {
    sequelize,
    modelName: 'Poll',
  });
  return Poll;
};