// Build a game board of squares to fit a given number of squares in width and height.
// Wish there would be a way to declare all those colors in css and have access in jQuery/JS.
var pieces = {
		/*
		 * Dimension of a piece on the board in px. Maybe should move this to the board.
		 */
		dim: 10,
		players: [ 
			/*
			 * Just defaults. See initialization function below. Real values, besides ID, come from CSS.
			 */
			{id: 'playerOne', occupied: '#cc0000', hit: '#ff4d4d', miss: '#ffcccc'},
			{id: 'playerTwo', occupied: '#0073e6', hit: '#66b3ff', miss: '#e6f2ff'},
			{id: 'playerThree', occupied: '#00cc00', hit: '#66ff66', miss: '#ccffcc'},
			{id: 'playerFour', occupied: '#ff9900', hit: '#ffc266', miss: '#ffebcc'},
		],
		init: function () {
			/*
			 * Naive, but maybe sufficient for 4 players.
			 */
			for (i = 0; i < this.players.length; i++) {
				var className = '.gameboard-' + this.players[i].id;
				var occupied = getStyleRuleValue('color', className);
				var hit = getStyleRuleValue('color', className+'-hits');
				var miss = getStyleRuleValue('color', className+'-miss');
				
				this.players[i].occupied = occupied;
				this.players[i].hit = hit;
				this.players[i].miss = miss;
			}
		}
}

// This is hard coded in the server, too. It depicts the size of the game board in slots/tiles.
var board = {
		dimW: 40,
		dimH: 20
}

// The registered player types. This list is used to build the options in the UI.
var playerTypes =  {
		types : [ 
	                { type : 'builtIn', data : '0', description : 'Built in: Stationary, fire only'},
	                { type : 'builtIn', data : '1', description : 'Built in: Balance moving and firing'},
	                { type : 'builtIn', data : '2', description : 'Built in: Spawn - avoid being killed'}
	            ],
	    addType : function (type, data, description) {
	    	this.types.push({type : type, data : data, description : description});
	    	this.buildUIOptions();
	    },
	    buildUIOptions : function () {
	    	for (var i = 0; i < pieces.players.length; i++) {
	    		var selectObj = $( '#' + pieces.players[i].id + 'Select' );
	    		selectObj = selectObj.empty();
	    		for (var ii = 0; ii < this.types.length; ii++) {
	    			if (ii == 0) {
	    				selectObj.append(new Option(this.types[ii].description, this.types[ii].data, true, true));
	    			} else {
	    				selectObj.append(new Option(this.types[ii].description, this.types[ii].data, false, false));
	    			}
	    		}
	    	}
	    }  
};

function addPlayerType() {
	var value = $('#addPlayerTypeInput').val();
	if (value.length > 0) {
		playerTypes.addType('url', value, 'Custom: ' + value);
	}
}

// Attempt to centralize the interface spec to the server.
function playerJson(type, data, id) {
	return { 'type' : type, 'data' : data, 'id' : i };
}

var serverUrl = "http://localhost:8080/simpleviruswar/rest/start";

// Allow a user to specify the url of a player in addition of using the built-in players.
function addCustomPlayer() {
	var x = document.getElementById("mySelect");
	var option = document.createElement("option");
	option.text = "Kiwi";
	x.add(option);
	
	
}

// Resets the players on the page to default css. Would love to find a way to do this all by css.
// Issue is the colors. See above.
function setPlayerSelectionColors() {
	for (i = 0; i < pieces.players.length; i++) {

		//$( "#" + pieces.players[i].id ).css("color", pieces.players[i].occupied);
		//$( "#" + pieces.players[i].id ).css("text-decoration", "none");
		//$( "#" + pieces.players[i].id ).css("text-align", "right");
		//$( "#" + pieces.players[i].id ).css("border-style", "none");
        
		$( "#" + pieces.players[i].id ).removeClass("gameboard-label-loser");
		$( "#" + pieces.players[i].id ).removeClass("gameboard-label-winner");
		
	}
}

// Extract data from the page and build the payload for the game run.
function buildPlayerJson() {

	var players = [];
	for (i = 0; i < pieces.players.length; i++) {
		var name = pieces.players[i].id;
		var value = $("#" + name + "Select" ).val();
        if (parseInt(value) > 3) {
        	var player = playerJson('url', value, i);
        	players.push(player);
        } else {
        	var player = playerJson('builtIn', value, i);
        	players.push(player);
        }
	}
	return players;
}

function startGame(id) {
	paintedBoard.init(id);
}

function runGame() {
	var payload = buildPlayerJson();
	
	setPlayerSelectionColors();
	
	$.ajax({
		type : "POST",
		cache : false,
		url : serverUrl,
		dataType : "json",
		contentType: 'application/json',
		data: JSON.stringify(payload),
		success : function(data) {
			paintedBoard.playGame(data);
			paintedBoard.clear();
		},
		error : function(xhr, status, error) {
			alert("error <" + xhr.responseText + "> status <" + status + "> error <" + error + ">");
		}
	});
}

// To be run at an interval and drive the update of the game area canvas.
// I would have kept this inside of the paintedBoard object. But on execution
// the scope of the object seem to be lost (moved to windows level). So I
// wanted to avoid confusion on the scope and moved it out of the object.
function updateGameArea() {
	if (paintedBoard.nextMove < paintedBoard.movesToPlay.length) {
		paintedBoard.playMove(paintedBoard.movesToPlay[paintedBoard.nextMove]);
		paintedBoard.nextMove++;
	}
}

var paintedBoard = {
		canvas : document.getElementById("myCanvas"),
		movesToPlay : JSON.parse("[]"),
		nextMove : 0,
	    init : function(id) {
	        this.canvas.width = pieces.dim * board.dimW;
	        this.canvas.height = pieces.dim * board.dimH;
	        this.context = this.canvas.getContext("2d");
	    },
	    clear : function() {
	        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
	    },
	    playGame : function(newMovesToPlay) {
	    	this.clear();
	    	this.movesToPlay = newMovesToPlay;
	    	this.nextMove = 0;
	    	this.interval = setInterval(updateGameArea, 20);
	    },
	    playMove : function(move) {
	    	if (move.impact == 'exit') {
    			this.context.clearRect(move.posW * pieces.dim, move.posH * pieces.dim, pieces.dim, pieces.dim);
    		} else {
    			ctx = this.canvas.getContext("2d");
    			switch (move.impact) {
	    		case 'hit' :
	    			ctx.fillStyle = pieces.players[move.player].hit;
	    			ctx.beginPath();
			        ctx.arc(
			        		(move.posW * pieces.dim) + pieces.dim/2,
			        		(move.posH * pieces.dim) + pieces.dim/2,
			        		pieces.dim/3,
			        		0,
			        		2*Math.PI);
			        ctx.fill();
	    			break;
	    		case 'miss':
	    			ctx.fillStyle = pieces.players[move.player].miss;
	    			ctx.beginPath();
			        ctx.arc(
			        		(move.posW * pieces.dim) + pieces.dim/2,
			        		(move.posH * pieces.dim) + pieces.dim/2,
			        		pieces.dim/3,
			        		0,
			        		2*Math.PI);
			        ctx.fill();
	    			break;
	    		case 'enter':
	    			ctx.fillStyle = pieces.players[move.player].occupied;
	    			ctx.fillRect(move.posW * pieces.dim, move.posH * pieces.dim, pieces.dim, pieces.dim);
	    			break;
	    		case 'lose':
	    			$( '#'+pieces.players[move.player].id ).addClass("gameboard-label-loser");
	    			//$( '#'+pieces.players[move.player].id ).css("text-decoration","inherit");
	    			//$( 'label[for=' + pieces.players[move.player].id + 'Select]' ).addClass("gameboard-label-loser");
	    			//$( '#'+pieces.players[move.player].id ).css("text-decoration", "line-through");
	    			break;
	    		case 'win':
	    			$( '#'+pieces.players[move.player].id ).addClass("gameboard-label-winner");
	    			//$( '#'+pieces.players[move.player].id ).css("border", "solid");
	    			break;
	    		default:
	    			break;
	    		}
    		}
    	
	    },
}
/*
 * Credit to rlemon <http://stackoverflow.com/questions/16965515/how-to-get-a-style-attribute-from-a-css-class-by-javascript-jquery>
 * 
 * Usage:
 * var color = getStyleRuleValue('color', '.foo'); // searches all sheets for the first .foo rule and returns the set color style.
 * var color = getStyleRuleValue('color', '.foo', document.styleSheets[2]); 
 * 
 */
function getStyleRuleValue(style, selector, sheet) {
    var sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
    for (var i = 0, l = sheets.length; i < l; i++) {
        var sheet = sheets[i];
        if( !sheet.cssRules ) { continue; }
        for (var j = 0, k = sheet.cssRules.length; j < k; j++) {
            var rule = sheet.cssRules[j];
            if (rule.selectorText.split(',').indexOf(selector) !== -1) {
                return rule.style[style];
            }
        }
    }
    return null;
}