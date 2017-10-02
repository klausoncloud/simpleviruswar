function setPicture(id, src) {
  document.getElementById(id).src=src;
}

var picture = ['imgs/IMG_2952.jpg', 'imgs/Klausjaws.jpg'], index = 0;

function initializePicture(id) {
	setPicture(id, picture[index])
}

function togglePicture(id) {
	if (index == 0) {
		index = 1;
	} else {
		index = 0;
	}
	setPicture(id, picture[index]);
}