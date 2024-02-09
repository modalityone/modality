var googleMap, java, redirectConsole = true;

(g=>{var h,a,k,p="The Google Maps JavaScript API",c="google",l="importLibrary",q="__ib__",m=document,b=window;b=b[c]||(b[c]={});var d=b.maps||(b.maps={}),r=new Set,e=new URLSearchParams,u=()=>h||(h=new Promise(async(f,n)=>{await (a=m.createElement("script"));e.set("libraries",[...r]+"");for(k in g)e.set(k.replace(/[A-Z]/g,t=>"_"+t[0].toLowerCase()),g[k]);e.set("callback",c+".maps."+q);a.src='https://maps.' + c + 'apis.com/maps/api/js?'+e;d[q]=f;a.onerror=()=>h=n(Error(p+" could not load."));a.nonce=m.querySelector("script[nonce]")?.nonce||"";m.head.append(a)}));d[l]?console.warn(p+" only loads once. Ignoring:",g):d[l]=(f,...n)=>r.add(f)&&u().then(()=>d[l](f,...n))})({
key: "YOUR_API_KEY",
 v: "3.55"
});

async function initMap() {
    const { Map } = await google.maps.importLibrary('maps');
    const { AdvancedMarkerElement, PinElement } = await google.maps.importLibrary('marker');
    googleMap = new Map(document.body, {
        center: { lat: 35, lng: -30 },
        zoom: 2,
        mapTypeControl: false,
        navigationControlOptions: {style: google.maps.NavigationControlStyle.SMALL},
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        mapId: 'DEMO_MAP_ID'
    });
    window.AdvancedMarkerElement = AdvancedMarkerElement;
    window.PinElement = PinElement;
    if (java && java.onGoogleMapLoaded)
        java.onGoogleMapLoaded();
}

function createJSObject() {
    return {};
}

function createMarker(lat, lng, title, javaMarker) {
    var marker = new AdvancedMarkerElement({
         map: googleMap,
         position: {lat: lat, lng: lng},
         title: title,
         content: new PinElement().element
    });
    marker.addListener('click', x => {
        java.onMarkerClicked(javaMarker);
    });
    return marker;
}

function injectJava(javaInstance) {
    java   = javaInstance;
    if (redirectConsole) {
        console.log   = function(message) { java.consoleLog(message);   };
        console.warn  = function(message) { java.consoleWarn(message);  };
        console.error = function(message) { java.consoleError(message); };
    }
    if (googleMap)
        java.onGoogleMapLoaded();
}

if (java)
    injectJava(java);

// Google map use ResizeObserver which is not supported by default on OpenJFX WebView, so we use a polyfill in that case
if (window.ResizeObserver) {
   initMap();
} else {
   var script = document.createElement('script');
   document.body.appendChild(script);
   script.onload = initMap;
   script.src = 'https://unpkg.com/resize-observer-polyfill@1.5.1/dist/ResizeObserver.global.js';
};
