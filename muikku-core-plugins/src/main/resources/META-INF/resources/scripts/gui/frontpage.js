(function() {
  
  $(document).ready(function(){
    $('.carousel-videos-wrapper').slick({
      appendDots: ".carousel-video-controls-wrapper",
      arrows: false,
      dots: true,
      dotsClass: "carousel-video-controls",
      fade: true,
      speed: 750,
      waitForAnimate: false
    });
  });
  
  $(document).ready(function(){
    $('.carousel-images-wrapper').slick({
      appendDots: ".carousel-image-controls-wrapper",
      arrows: false,
      dots: true,
      dotsClass: "carousel-image-controls",
      fade: true,
      speed: 750,
      swipeToSlide: true,
      waitForAnimate: false
    });
  });

}).call(this);