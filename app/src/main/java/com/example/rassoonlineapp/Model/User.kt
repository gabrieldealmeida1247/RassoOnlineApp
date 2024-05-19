//package com.example.rassoonlineapp.Model
/*
class User {
    private var username: String = ""
    internal var fullname: String = ""
    private var bio: String = ""
    private var image: String = ""
    internal var uid: String = ""
    private var description: String = ""
    private var especialidade: String = ""

    constructor()

    constructor(username: String, fullname: String, bio: String,  image: String, uid: String, description: String, especialidade: String){
        this.username = username
        this.fullname = fullname
        this.bio = bio
        this.image = image
        this.uid = uid
        this.description = description
        this.especialidade = especialidade
    }

    fun  getUsername(): String {
        return  username
    }

    fun  setUsername(username: String) {

        this.username = username
    }



    fun  getFullname(): String {
        return  fullname
    }

    fun  setFullname(fullname: String) {

        this.fullname = fullname
    }


    fun  getBio(): String {
        return  bio
    }

    fun  setBio(bio: String) {

        this.bio = bio
    }


    fun  getImage(): String {
        return  image
    }

    fun  setImage(image: String) {

        this.image = image
    }


    fun  getUID(): String {
        return  uid
    }

    fun  setUID(uid: String) {

        this.uid = uid
    }

    fun  getDescription(): String {
        return  description
    }

    fun  setDescription(description: String) {

        this.description = description
    }

    fun getEspecialidade(): String{
        return  especialidade
    }


    fun setEspecialidade(especialidade: String){
        this.especialidade = especialidade
    }



}

 */


package com.example.rassoonlineapp.Model

class User {
    private var username: String = ""
    internal var fullname: String = ""
    private var bio: String = ""
    private var image: String = ""
    internal var uid: String = ""
    private var description: String = ""
    private var especialidade: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    constructor()

    constructor(username: String, fullname: String, bio: String, image: String, uid: String, description: String, especialidade: String, latitude: Double, longitude: Double) {
        this.username = username
        this.fullname = fullname
        this.bio = bio
        this.image = image
        this.uid = uid
        this.description = description
        this.especialidade = especialidade
        this.latitude = latitude
        this.longitude = longitude
    }

    fun getUsername(): String {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getFullname(): String {
        return fullname
    }

    fun setFullname(fullname: String) {
        this.fullname = fullname
    }

    fun getBio(): String {
        return bio
    }

    fun setBio(bio: String) {
        this.bio = bio
    }

    fun getImage(): String {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }

    fun getUID(): String {
        return uid
    }

    fun setUID(uid: String) {
        this.uid = uid
    }

    fun getDescription(): String {
        return description
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun getEspecialidade(): String {
        return especialidade
    }

    fun setEspecialidade(especialidade: String) {
        this.especialidade = especialidade
    }

    fun getLatitude(): Double {
        return latitude
    }

    fun setLatitude(latitude: Double) {
        this.latitude = latitude
    }

    fun getLongitude(): Double {
        return longitude
    }

    fun setLongitude(longitude: Double) {
        this.longitude = longitude
    }
}
