import React from "react";
import { LocalPharmacy } from "./LocalPharmacy";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalPharmacy",
  component: LocalPharmacy,
};

export const Default = () => <LocalPharmacy />;
export const Fill = () => <LocalPharmacy fill="blue" />;
export const Size = () => <LocalPharmacy height="50" width="50" />;
export const CustomStyle = () => <LocalPharmacy style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalPharmacy className="custom-class" />;
