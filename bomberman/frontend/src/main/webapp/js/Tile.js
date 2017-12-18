Tile = Entity.extend({
    /**
     * Entity position on map grid
     */
    position: {},

    /**
     * Bitmap dimensions
     */
    size: {
        w: 32,
        h: 32
    },

    /**
     * Bitmap animation
     */
    bmp: null,

    material: '',

    init: function(id, material, position) {
        this.id = id;
        this.material = material;
        this.position = position;
        var img;
        if (material === 'Grass') {
            img = gGameEngine.tilesImgs.grass;
        } else if (material === 'Wall') {
            img = gGameEngine.tilesImgs.wall;
        } else if (material === 'Wood') {
            img = gGameEngine.tilesImgs.wood;
        }
        this.bmp = new createjs.Bitmap(img);

        this.bmp.x = position.x;
        this.bmp.y = position.y;

        gGameEngine.stage.addChild(this.bmp);
    },

    update: function(material) {
        var id = this.id;
        var position =  this.position;
        var index = gGameEngine.stage.getChildIndex(this.bmp);
        gGameEngine.stage.removeChild(this.bmp);
        var img;
        if (material === 'Grass') {
            img = gGameEngine.tilesImgs.grass;
        } else if (material === 'Wall') {
            img = gGameEngine.tilesImgs.wall;
        } else if (material === 'Wood') {
            img = gGameEngine.tilesImgs.wood;
        }
        this.material = material;
        this.bmp = new createjs.Bitmap(img);
        this.bmp.x = position.x;
        this.bmp.y = position.y;
        gGameEngine.stage.addChild(this.bmp);
        for (var i = 0; i < gGameEngine.players.length; i++) {
            var player = gGameEngine.players[i];
            gGameEngine.stage.removeChild(player.bmp);
            gGameEngine.stage.addChild(player.bmp);
        }
    },

    remove: function() {
        gGameEngine.stage.removeChild(this.bmp);
    }
});