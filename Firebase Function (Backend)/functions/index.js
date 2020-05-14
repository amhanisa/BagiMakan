const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();

exports.kurangiJumlah = functions.firestore
    .document('makanan/{makananId}/request/{reqId}')
    .onUpdate((change, context) => {
        const request = change.after.data();
        const status = request.status;
        const minta = request.jumlah;

        if (status === "Disetujui") {

            const parent = change.after.ref.parent.parent;

            parent.get()
                .then(parentSnap => {
                    const sisa = parentSnap.data().jumlah;
                    const final = sisa - minta;

                    return parent.update({
                        jumlah: final
                    });

                })
                .catch(err => {
                    console.log("FAK", err);
                });
        } else {
            return "ditolak";
        }
    });

exports.sendNotificationResult = functions.firestore
    .document('makanan/{makananId}/request/{reqId}')
    .onUpdate((change, context) => {
        const request = change.after.data();
        const status = request.status;
        const userId = request.userId;

        if (status === "Disetujui") {

            const makanan = change.after.ref.parent.parent;

            makanan.get()
                .then(makananSnap => {

                    const makananKey = makananSnap.id;
                    const makananUserId = makananSnap.data().userId;
                    const makananName = makananSnap.data().name;
                    const makananJumlah = request.jumlah;

                    const message = 'Selamat anda mendapatkan ' + makananJumlah + ' ' + makananName + '. Silakan kontak pemilik makanan';


                    return db.collection('users').doc(userId)
                        .get()
                        .then(userSnap => {
                            const token = userSnap.data().token;

                            const payload = {
                                data: {
                                    title: 'Permintaan anda disetujui',
                                    body: message,
                                    click_action: '.activity.DETAIL_MAKANAN',
                                    makananKey: makananKey,
                                    userId: makananUserId
                                }
                            };

                            return admin.messaging().sendToDevice(token, payload);

                        })
                        .catch(err => {
                            console.log("Error getting document", err);
                        });


                })
                .catch(err => {
                    console.log("GAGAL", err);
                });



        } else {
            return "ditolak";
        }
    });

exports.sendNotificationRequest = functions.firestore
    .document('makanan/{makananId}/request/{reqId}')
    .onCreate((snapShot, context) => {
        const request = snapShot.data();
        const userId = request.userId;

        const makanan = snapShot.ref.parent.parent;

        makanan.get()
            .then(makananSnap => {

                const makananKey = makananSnap.id;
                const makananUserId = makananSnap.data().userId;
                const makananName = makananSnap.data().name;
                const makananJumlah = request.jumlah;

                const message = 'Ada yang meminta ' + makananJumlah + ' ' + makananName + '.  Tolong dicek';

                return db.collection('users').doc(makananUserId)
                    .get()
                    .then(userSnap => {
                        const token = userSnap.data().token;

                        const payload = {
                            data: {
                                title: 'Ada permintaan makanan',
                                body: message,
                                click_action: '.activity.DETAIL_MAKANAN',
                                makananKey: makananKey,
                                userId: makananUserId
                            }
                        };

                        return admin.messaging().sendToDevice(token, payload);

                    })
                    .catch(err => {
                        console.log("Error getting document", err);
                    });


            })
            .catch(err => {
                console.log("GAGAL", err);
            });
    });

exports.deletesubcollectionmakanan = functions.firestore
    .document('makanan/{makananId}')
    .onDelete((snap, context) => {
        const deletedValue = snap.data();

        let collectionRequest = db.collection('makanan').doc(deletedValue.id).collection('request');

        let allRequest = collectionRequest.get()
            .then(snapshot => {
                snapshot.forEach(doc => {
                    db.collection('makanan').doc(deletedValue.id).collection('request').doc(doc.id).delete();
                });

                return snapshot.data();
            })
            .catch(err => {
                console.log('Error getting documents', err);
            });
    });