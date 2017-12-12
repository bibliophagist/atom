Bonus = Entity.extend({
    types: ['speed', 'bomb', 'fire'],

    type: '',
    position: {},
    bmp: null,

    /* init: function(position, typePosition) {
         this.type = this.types[typePosition];

         this.position = position;

         this.bmp = new createjs.Bitmap(gGameEngine.bonusesImg);
         var pixels = Utils.convertToBitmapPosition(position);
         this.bmp.x = pixels.x;
         this.bmp.y = pixels.y;
         this.bmp.sourceRect = new createjs.Rectangle(typePosition * 32, 0, 32, 32);
         gGameEngine.stage.addChild(this.bmp);*/

    init: function (id, type , position) {
        this.id = id;
        this.type = type;
        this.position = position;

        console.log("trying to create bonus!");

        var typePosition;
        if (type === 'speed') {
            typePosition = 0;
        } else if (type === 'bomb') {
            typePosition = 1;
        } else if (type === 'fire') {
            typePosition = 2;
        }

        console.log(typePosition);

        this.bmp = new createjs.Bitmap(gGameEngine.bonusesImg);
        this.bmp.x = position.x;
        this.bmp.y = position.y;
        this.bmp.sourceRect = new createjs.Rectangle(typePosition * 32, 0, 32, 32);

        gGameEngine.stage.addChild(this.bmp);
    },

    update: function() {
    },

    remove: function () {
        gGameEngine.stage.removeChild(this.bmp);
    }
});