const Loading = document.querySelector("#loading-overlay");

// backend base URL
const IPAddress = "http://localhost:8080";

// Thymeleaf injected value (safe way)
//let Profile = document.body.dataset.profile;

// ================= PROFILE =================
function setRetailerProfile() {
    if (Profile) {
        document.querySelector(".user-pic > img").src =
            `${IPAddress}/profile/${Profile}`;

        document.querySelector(".user-pic").style.display = "flex";
        document.querySelector(".user-icon").style.display = "none";
    }
}

// ================= PRODUCTS =================
async function GettingAllProducts() {
   
    let RetailerID = document.querySelector(".id").innerText.trim();

    let Payload = {
        INSTRUCTION: "GET-MY-PRODUCTS",
        User_id: RetailerID
    };

    Loading.style.display = "flex";

    let Account_Products = await fetchData(Payload);
    
    if (Array.isArray(Account_Products) && Account_Products.length !== 0) {

        document.querySelector("main").innerHTML = "";

        const fragment = document.createDocumentFragment();

        Account_Products.forEach(product => {

            const ProductCard = document.createElement("div");
            ProductCard.className = "a-product";

            ProductCard.innerHTML = `
                <img src="${IPAddress}/products/${product.Url}" alt="">
                <p class="a-prod-name">${product.name}</p>
                <p class="a-prod-price">GHC : ${product.price}</p>
                <p class="a-prod-description">${product.description}</p>
                <div class="prouduct-cart-bottom">
                    <p class="posted-at">Posted ${product.postedAt}</p>
                    <div class="order"><i class="fa-solid fa-shopping-cart"></i></div>
                </div>
            `;

            fragment.appendChild(ProductCard);
        });

        document.querySelector("main").appendChild(fragment);
    }

    Loading.style.display = "none";
}

// ================= INIT =================
setRetailerProfile(Profile);
GettingAllProducts();

// ================= FETCH =================
async function fetchData(payload) {
    try {
        const response = await fetch(`${IPAddress}/api/process`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error(`Network Error: ${response.status}`);

        return await response.json();

    } catch (err) {
        console.error("Fetch error:", err);
        alert("Error occurred. Check connection.");
        Loading.style.display = "none";
        return null;
    }
}