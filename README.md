# 🎵  Altas-Notas
Music Player based on Firebase Services


<br /><br/>
## 🕹️ Instalation and How to Use
Export project from Zip and Import full folder


<br /><br/>


## 💻 Progress
 ![Progress](https://progress-bar.dev/100/?title=v1.00&width=200&color=B076CA)
 
 ![Progress](https://progress-bar.dev/100/?title=v1.10&width=200&color=9D55BD)
 
 ![Progress](https://progress-bar.dev/80/?title=v1.50&width=200&color=730DA1)


## 👾 Tech Stack
* Java
* MVVM
* Firebase services
* ExoPlayer
* Foreground Service
* Facebook login integration 
* Google login integration 



## 🚀 v2.0
* SearchView connected with Albums and Songs from Firebase
* Change songs by Swipe
* Short links of Songs (Firebase dynamic links)
* Two Themes
* Last albums played
* Navigation Component
* Edit Playlist's Title and Description
* Sorting Song Order in Playlist
* Custom Progress bar's while fetching Firebase Data
* Artist Page
* Multiple Artist's at same song
* Diffrent Layout for diffrent phone's


<br /><br/>
## 📷 Screenshots

<p align="center">
<img src="video.gif" width="270px">
</p>

## 🌊 Details


### Setup[0] -  Run this project in Your envoirment


If You want to recreate my project in Your own envoirment,

<p>You need's to add new file ( google-services.json ) which allows connection with Firebase services. </p>
<p>For security reasons I let this file hidden.</p>
<p>Tutorial how to connect it : <a href="https://firebase.google.com/docs/android/setup"> ✨ Tutorial ✨ </a></p>


### Setup[1] - Realtime DB

In 4 diffrent picture's I want to show how I made DB structure


<div align="center"> <img  src="photo_1.png" height="300" /> </div>
<br/>
 <p>Photo above show's summary structure. I divided data to those catagories. </p>
<br/><br/>




<div> <img  src="photo_2.png" height="300"  /> </div>
<br/>
<p>Every song that is added to favorites list is added to List where title of List is ID of current user. </p>
<p>Every song here have different ID make randomly  </p>
<br/><br/>


<div> <img  src="photo_3.png" height="300" /> </div>
 <br/>
 <p>Here's how I store album data. It only part which You Have to COPY</p>
 <p>Without filling at least one album in Your DB, There will be none to download from.</p>
 <br/><br/>


<div> <img  src="photo_4.png" height="300" /> </div>
 <br/>
 <p>Thats how every song look like. Their dir name is irrevelant. </p>
 <p>Most important is their 3 (or 4 sometimes) values </p>
 
 *  title
 *  order
 *  path
 *  videoPath 

<p>videoPath doesnt always need to be, but if present - music player will play this video in Background</p>
 <br/><br/>



<p align="center">
<a href="https://api.codetabs.com/v1/loc?github=polonez-byte-112/Altas-Notas">Click here for code stats</a>
</p>
