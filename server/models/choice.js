'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  class Choice extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
      //A...Polls->Choices(1:m)
      this.belongsTo(models.Poll,{
        foreignKey:'pollId',
        as:'poll',
       
       
      })
    }
  };
  Choice.init({
    option: DataTypes.STRING,
    votes: DataTypes.INTEGER,
    pollId: DataTypes.INTEGER
  }, {
    sequelize,
    modelName: 'Choice',
  });
  return Choice;
};