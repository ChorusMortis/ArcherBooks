package com.mobdeve.s12.mco

class UserGenerator {

    companion object {
        private val firstNames = arrayListOf("Sarah", "Patrick", "Kevin", "Spencer", "Tricia", "Stacey", "Cassandra", "Robert", "Harley", "Luis")
        private val lastNames = arrayListOf("Dy", "Ong", "Martin", "Reyes", "Delos Santos", "Delos Reyes", "Wang", "Evangelista", "Curtis", "Carballo")

        fun generateSampleUsers(): ArrayList<UserModel> {
            val data = ArrayList<UserModel>()

            for(index in 0..9)
            {
                data.add(UserModel("user_${index}", firstNames[index], lastNames[index], "${firstNames[index].trim().lowercase()}_${lastNames[index].trim().lowercase()}@dlsu.edu.ph", UserModel.SignUpMethod.EMAIL))
            }

            return data
        }
    }
}
