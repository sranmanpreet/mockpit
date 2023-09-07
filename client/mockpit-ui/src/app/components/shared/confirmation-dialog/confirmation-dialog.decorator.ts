import { ConfirmationDialogService } from "src/app/components/shared/confirmation-dialog/confirmation-dialog.service";
import { ConfirmationDialogData } from "./confirmation-dialog-data";
import { ConfirmationDialogComponent } from "./confirmation-dialog/confirmation-dialog.component";

const defaultConfirmData = {
    title: "Confirmation",
    message: "Are you sure you want to perform this action?"
}


export function needConfirmation ( confirmData : ConfirmationDialogData = defaultConfirmData) {

    return function (target: Object, propertyKey: string, descriptor: PropertyDescriptor) {
        const originalMethod = descriptor.value;

        descriptor.value = async function (...args: any) {
            ConfirmationDialogService.getInstance()?.openDialog(confirmData,ConfirmationDialogComponent).subscribe((validation: any) => {
                if (validation){
                    originalMethod.apply(this, args);
                }
              });
        };

        return descriptor;
    };

}