function handlePostInSeparateRoute(form) {
    // enable this if model prediction takes too long
    /*
    let textContent = form.children[0].value;
    fetch("/make-post", {
        method: "POST",
        body: JSON.stringify({content: textContent}),
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        }
    }).then(res => {
        console.log("Make post request complete! response:", res);
    });*/
}

function openEditor(postID) {
    let toEdit = document.getElementById("popup");
    let view = toEdit.getElementsByClassName("view").item(0);
    let edit = toEdit.getElementsByClassName("edit").item(0);
    view.style.display = "none";
    view.style.opacity = "0";
    view.className = "view hidden";
    edit.style.display = "block";
    edit.style.opacity = "1";
    edit.className = "edit";
}

function closeEditor(postID) {
    let toEdit = document.getElementById("popup");
    let view = toEdit.getElementsByClassName("view").item(0);
    let edit = toEdit.getElementsByClassName("edit").item(0);
    view.style.display = "block";
    view.style.opacity = "1";
    view.className = "view";
    edit.style.display = "none";
    edit.style.opacity = "0";
    edit.className = "edit hidden";
}

function expand(id) {
    let popup = document.getElementById("popup");
    if (popup) { return; }
    let expander = document.getElementById(id);
    let clone = expander.cloneNode(true);
    clone.classList.add("expanded");
    clone.id = "popup";
    clone.removeAttribute("onclick");
    // Note: Post form has "shrunken" class
    let elems = clone.getElementsByClassName("shrunken");
    for (let i = 0; i < elems.length; i++) {
        let elem = elems.item(i);
        elem.style.display = "block";
        elem.style.opacity = "1";
        elem.classList.remove("shrunken");
    }

    expander.parentNode.appendChild(clone);
    setTimeout(() => document.addEventListener('click', popupClick), 10);
}

function popupClick(event) {
    let popup = document.getElementById("popup");
    if (!popup) { return; }
    if (event.target instanceof Node) {
        if (!popup.contains(event.target)) {
            popup.remove();
            document.removeEventListener('click', popupClick);
        }
    }
}