const Loading = document.querySelector("#loading-overlay");
const Main = document.querySelector(".main");
const Cart_Overlay = document.querySelector(".cart-overlay");
const Payment_Overlay = document.querySelector(".payment-overlay");
const NoProducts = document.querySelector(".no-found-products");
const OrderSuccess = document.querySelector(".success-overlay");
const Call = document.querySelector("#call");
const PhoneNumber = document.querySelector(".phone");
let product_ID = null;


const toast = document.querySelector(".toast");
const toastIcon = document.querySelector(".toast-icon > i");
const toastHeader = document.querySelector(".toast-content > h4");
const toastText = document.querySelector(".toast-text");


// backend base URL
const IPAddress = "http://localhost:8080";

// ================= PROFILE =================
function setRetailerProfile() {
    if (Profile) {
        document.querySelector(".user-pic > img").src =
            `${IPAddress}/profile/${Profile}`;

        document.querySelector(".user-pic").style.display = "flex";
        document.querySelector(".user-icon").style.display = "none";
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    toast.classList.add("hide");
});

function showToast(icon, header, text, iconColor) {
    toastIcon.className = "toast-icon"; // safe reset
    toastIcon.className = "";
    icon.split(" ").forEach(cls => {
        toastIcon.classList.add(cls);
    });

    toastIcon.style.color = iconColor;
    toastHeader.textContent = header;
    toastText.textContent = text;
    toast.classList.remove("hide");

    setTimeout(() => {
        toast.classList.add("show");
    }, 100);

    setTimeout(() => {
        toast.classList.remove("show");
        toast.classList.add("hide");
    }, 3000);
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
            ProductCard.dataset.productId = product.Id;

            ProductCard.innerHTML = `
                <img src="${IPAddress}/products/${product.Url}" alt="" class="prod-img">
                <p class="a-prod-name">${product.name}</p>
                <p class="a-prod-price">${product.currencyCode}: ${product.price}</p>
                <p class="a-prod-description">${product.description}</p>
                <div class="prouduct-cart-bottom">
                    <p class="posted-at">Posted ${product.postedAt}</p>
                    <div class="order"><i class="fa-solid fa-shopping-cart"></i></div>
                </div>
            `;

            fragment.appendChild(ProductCard);
        });

        document.querySelector("main").appendChild(fragment);
    } else {
        NoProducts.style.display = "flex";
    }

    Loading.style.display = "none";
}

// ================= INIT =================
setRetailerProfile(Profile);
GettingAllProducts();

Call.addEventListener("click", () => {
    let phoneNumber = PhoneNumber.textContent;
    window.location.href = `tel:${phoneNumber}`;
});

// ====== Showing Payment and order cart
Main.addEventListener("click", async e => {
    if (e.target.closest(".order")) {

      
        const productCard = e.target.closest(".a-product");
        product_ID = productCard.dataset.productId;
        const productName = productCard.querySelector(".a-prod-name").textContent;
        const productPrice = productCard.querySelector(".a-prod-price").textContent;
        const productDiscripion = productCard.querySelector(".a-prod-description").textContent;
        const retailerName = document.querySelector(".name").innerText.trim();
        const restailerID = document.querySelector(".id").innerText.trim();
        const retailerEmail = document.querySelector(".email").innerText.trim();
        const retailerPhone = document.querySelector(".phone").innerText.trim();
        const productImage = productCard.querySelector(".prod-img").src;
        const retailer_Profile_Pic = document.querySelector(".user-pic > img").src;

        Cart_Overlay.querySelector(".cart-retailer__image").src = retailer_Profile_Pic;
        Cart_Overlay.querySelector(".cart-product__image").src = productImage;
        Cart_Overlay.querySelector(".cart-product__name").textContent = productName;
        Cart_Overlay.querySelector(".cart-product__price").textContent = productPrice;
        Cart_Overlay.querySelector(".cart-product__description").textContent = productDiscripion;
        Cart_Overlay.querySelector(".cart-retailer__name").textContent = retailerName;
        Cart_Overlay.querySelector(".cart-retailer__email").textContent = retailerEmail;
        Cart_Overlay.querySelector(".cart-retailer__phone").textContent = retailerPhone;
        Cart_Overlay.querySelector(".total-amount").textContent = productPrice;

        let unitePrice = parseFloat(productPrice.replace(/[^\d.]/g, ""));
        let counryCode = productPrice.split(":")[0].trim();
        let total = 0;

        // ============= cancelling Crat =========
        Cart_Overlay.querySelector(".cart-close-btn").onclick = () => {
            Cart_Overlay.style.display = "none";
        }

        Cart_Overlay.querySelector(".minus").onclick = () => {
            let productQuatity = Cart_Overlay.querySelector(".qty-number").textContent.trim();
            if (productQuatity > 1) {
                productQuatity--;

                total = productQuatity * unitePrice;
                Cart_Overlay.querySelector(".total-amount").textContent = `${counryCode} : ${total}`;
                Cart_Overlay.querySelector(".qty-number").textContent = productQuatity;
            }
        }

        //======== executing plus btn =========
        Cart_Overlay.querySelector(".plus").onclick = () => {
            let productQuatity = Cart_Overlay.querySelector(".qty-number").textContent.trim();
            productQuatity++;
            total = productQuatity * unitePrice;
            Cart_Overlay.querySelector(".total-amount").textContent = `${counryCode}: ${total}`;
            Cart_Overlay.querySelector(".qty-number").textContent = productQuatity;
        }

        Cart_Overlay.querySelector(".cart-buy-btn").onclick = () => {
            Cart_Overlay.style.display = "none";
            Payment_Overlay.style.display = "flex";
            Payment_Overlay.querySelector(".customer-number-input").value = null;
        }
        
        let inflatePayload = {
            INSTRUCTION: "INFLATE-TRY-TO-BUY",
            productID: product_ID
        }

        let inflateResult = null;
        try {
            Loading.style.display = "flex";
            inflateResult = await fetchData(inflatePayload);
            Loading.style.display = "none";
        } catch (err) {
            showToast(
                "fa-solid fa-exclamation",
                "Error",
                "Sorry error occurred",
                "red"
            );
            return;
        }

        if (inflateResult && inflateResult.status === "OK") {
            Cart_Overlay.style.display = "flex";
        }
    }
});

Payment_Overlay.querySelector(".cancel-purchase").addEventListener("click", () => {
    Cart_Overlay.style.display = "flex";
    Payment_Overlay.style.display = "none";
});

Payment_Overlay.querySelector(".purchase-btn").addEventListener("click", async () => {
    if (!iti.isValidNumber()) {
        alert("Please enter a valid phone number");
        return;
    }

    let Phone = iti.getNumber();
    let newProductPrice = parseFloat(Cart_Overlay.querySelector(".cart-product__price").textContent.replace(/[^\d.]/g, ""));
    let Payload = {
        INSTRUCTION: "PLACE-ORDER",
        ProductId: product_ID,
        Quantity: Cart_Overlay.querySelector(".qty-number").textContent,
        CustomerPhone: Phone,
        ProductName: Cart_Overlay.querySelector(".cart-product__name"),
        ProductPrice: newProductPrice
    }

    try {
        Loading.style.display = "flex";
        let Result = null;
        try {
            Result = await fetchData(Payload);
        } catch (err) {
            showToast(
                "fa-solid fa-exclamation",
                "Error",
                "Error Occured",
                "red"
            );
        }
        Loading.style.display = "none";

        if (Result && Result.status === "OK") {
            Payment_Overlay.style.display = "none"
            OrderSuccess.querySelector("#display-order-id").textContent = "#" + Result["orderID"];
            OrderSuccess.style.display = "flex";
        }
    } catch (err) {
        Loading.style.display = "none";
        alert("Network error");
        console.error(err);
    }
});

OrderSuccess.querySelector(".success-close-btn").addEventListener("click", () => {
    OrderSuccess.style.display = "none";
});


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
        Loading.style.display = "none";
        throw err;
    }
}

const iti = window.intlTelInput(Payment_Overlay.querySelector(".customer-number-input"), {
    initialCountry: "auto",
    geoIpLookup: function (callback) {
        fetch("https://ipapi.co/json")
            .then(res => res.json())
            .then(data => callback(data.country_code))
            .catch(() => callback("us"));
    },
    separateDialCode: true,
    useFullscreenPopup: false,
    utilsScript: "https://cdn.jsdelivr.net/npm/intl-tel-input@19.5.5/build/js/utils.js"
});
