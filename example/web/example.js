//-----------------------------------------------------------------------------
// Gradle Application Plugin
// Copyright 2010-2024 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

loadImageFromScript = function() {
    const image = document.createElement("img");
    image.addEventListener("load", () => {
        const description = "Loaded image size: " + image.width + "x" + image.height;
        document.getElementById("ajaxImage").innerText = description;
    });
    image.src = "images/icon.png";
};

window.shareText = function() {
    const field = document.getElementById("shareText");

    if (navigator.share) {
        navigator.share({
            title: "Share text",
            text: field.innerText
        });
    }
};

window.openInNativeBrowser = function(url) {
    if (window.clrz) {
        window.clrz.openNativeBrowser(url);
    } else {
        window.location.href = url;
    }
};

window.loadStorageText = function() {
    const field = document.getElementById("storageField");

    if (window.clrz) {
        window.clrz.loadPreferences();
        field.value = "Native: " + localStorage.getItem("test");
    } else {
        field.value = "Local: " + localStorage.getItem("test");
    }
};

window.saveStorageText = function() {
    const field = document.getElementById("storageField");
    localStorage.setItem("test", field.value);
    if (window.clrz) {
        window.clrz.savePreferences("test", field.value);
    }
};

document.addEventListener("DOMContentLoaded", event => {
    loadImageFromScript();
    if (!navigator.share) {
        document.getElementById("shareText").placeholder = "Not available";
    }
});
