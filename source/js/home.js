var images=[
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/0.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/1.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/2.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/3.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/4.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/5.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/6.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/7.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/8.jpg",
		"http://7xrszw.com1.z0.glb.clouddn.com/github/home/images/wallpaper/9.jpg"
	];
setTimeout(function () {
	var nav = document.getElementById("nav");
	nav.style.marginTop = "18%";
	var img = new Image();
	img.src = images[parseInt(Math.random() * 10)];
	img.onload = function () {
		document.body.style.backgroundImage = 'url("' + img.src + '")';
		document.getElementById("loading").style.display = "none";
	}
}, 0)