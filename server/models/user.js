'use strict';
const {
  Model
} = require('sequelize');
module.exports = (sequelize, DataTypes) => {
  
  class User extends Model {
    /**
     * Helper method for defining associations.
     * This method is not a part of Sequelize lifecycle.
     * The `models/index` file will call this method automatically.
     */
    static associate(models) {
      // define association here
      this.hasMany(models.Poll,{
        foreignKey:'userId',
        as:'polls',
         onDelete:'CASCADE',
         onUpdate:'CASCADE',

       
       
      })
    }
  };
  User.init({
    username: DataTypes.STRING,
    email: DataTypes.STRING,
    password:DataTypes.STRING,
    temporarytoken:DataTypes.STRING,
    
    verified:{
      type:DataTypes.STRING,
      defaultValue:false
    },
    role:{
      type: DataTypes.ENUM,
      values:["user","admin"],
      defaultValue:"user"
      }
  }, {
    sequelize,
    modelName: 'User',
  });
  return User;
};