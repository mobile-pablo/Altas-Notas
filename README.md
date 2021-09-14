<div> <img  src="readme_media/mockup_two.png" width="100%"  /> </div>

# 🎵  Altas-Notas
Music Player based on Firebase Services



## 💻 Progress
 ![Progress](https://progress-bar.dev/100/?title=v1.00&width=200&color=B076CA)
 
 ![Progress](https://progress-bar.dev/100/?title=v1.10&width=200&color=9D55BD)
 
 ![Progress](https://progress-bar.dev/100/?title=v1.50&width=200&color=730DA1)
<br/><br/>



## 👾 Tech Stack
* Java
* MVVM
* Firebase services
* ExoPlayer
* Foreground Service
* Facebook login integration 
* Google login integration 
* ViewBinding 
<br /><br/>
<br /><br/>


## 🎥 Preview

<p align="center">
<img src="readme_media/video.gif" width="270px">
</p>

<br /><br/>
<br /><br/>


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
* Different Layout for different phone's


<br /><br/>


## ⚠️  Warning

<p>This Project is currently limited by Firebase download bandwith 
as seen image below</p>
<p> Becouse of this App may not be working correctly If Bandwith is used.</p>

<div> <img  src="readme_media/firebase_usage.png" width="100%"  /> </div>
<br/><br/>


## 🌊 Instalation 

<div> <img  src="readme_media/mockup.png" width="100%"  /> </div>


### Setup[0] -  Run this project in Your envoirment
<br/>
<p>If You want to recreate my project in Your own envoirment,</p>
<p>Copy link and open Android Studio. Click 'Get from Version Control' and paste link to automaticly download project.</p>
<p>After that import full folder to Your Android Studio</p>
<br/><br/>
<p>You need's to add new file ( google-services.json ) which allows connection with Firebase services. </p>
<p>For security reasons I let this file hidden.</p>
<p>Tutorial how to connect it : <a href="https://firebase.google.com/docs/android/setup"> ✨ Tutorial ✨ </a></p>

<br/><br/><br/>
### Setup[1] - Realtime DB

In 4 different picture's I want to show how I made DB structure


<div> <img  src="readme_media/photo_1.png" height="300" /> </div>
<br/>
 <p>Photo above show's summary structure. I divided data to those catagories. </p>
<br/><br/>




<div> <img  src="readme_media/photo_2.png" height="300"  /> </div>
<br/>
<p>Every song that is added to favorites list is added to List where title of List is ID of current user. </p>
<p>Every song here have different ID make randomly  </p>
<br/><br/>


<div> <img  src="readme_media/photo_3.png" height="300" /> </div>
 <br/>
 <p>Here's how I store album data. It only part which You Have to COPY</p>
 <p>Without filling at least one album in Your DB, There will be none to download from.</p>
 <br/><br/>


<div> <img  src="readme_media/photo_4.png" height="300" /> </div>
 <br/>
 <p>Thats how every song look like. Their dir name is irrevelant. </p>
 <p>Most important is their 3 (or 4 sometimes) values </p>
 
 *  title
 *  order
 *  path
 *  videoPath 

<p>videoPath doesnt always need to be, but if present - music player will play this video in Background</p>
 <br/><br/>



## 📈 Stats
<a href="https://api.codetabs.com/v1/loc?github=polonez-byte-112/Altas-Notas">Click here for code stats</a>


##  <img  src="readme_media/law_icon.png" width="30" /> License
[![license](https://img.shields.io/github/license/DAVFoundation/captain-n3m0.svg?style=flat-square)](https://github.com/polonez-byte-112/Altas-Notas/blob/master/LICENSE)

